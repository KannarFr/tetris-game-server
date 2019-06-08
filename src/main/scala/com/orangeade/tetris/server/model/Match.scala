package com.orangeade.tetris.server.model

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Try, Success, Failure }

import play.api.db._
import play.api.Logger
import play.api.libs.json._

import akka.NotUsed
import akka.actor._
import akka.event.Logging
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Source, Sink }
import akka.util.Timeout

import anorm._
import anorm.SqlParser._

import com.orangeade.tetris.game.GameEngine
import com.orangeade.tetris.game.model.{ Point, RandomStoneFactory, Size }
import com.orangeade.tetris.server.actor._
import com.orangeade.tetris.server.model.player_module._
import com.orangeade.tetris.server.model.pg.pg_entity_module._
import com.orangeade.tetris.server.model.pg.AnormType._

object match_module {
  implicit val inEventFormat = Json.format[InEvent]
  implicit val sizeFormat = Json.format[Size]
  implicit val pointFormat = Json.format[Point]

  implicit val matchEngineWrites = new Writes[GameEngine] {
    def writes(x: GameEngine): JsValue = Json.toJson(x.points)
  }

  type Boards = Map[Player, GameEngine]

  final case class Match(
    id: String,
    label: Option[String],
    boards: Boards,
    events: Flow[MatchEvent, JsValue, _]
  ) {
    def serialize = MatchView(id, label, boards)
  }

  final case class MatchView(
    id: String,
    label: Option[String],
    boards: Boards,
  )
  implicit val matchViewWrites = Json.writes[MatchView]

  final case class WannaBeMatch(
    label: Option[String],
    players: List[Player],
    size: Size
  )
  implicit val wannaBeMatchReads = Json.reads[WannaBeMatch]

  class MatchDAO @Inject()(
    implicit system: ActorSystem,
    implicit val ec: ExecutionContext,
    materializer: Materializer,
  ) {
    // matches are stored in RAM, so we only keep running matches
    private var matches = List.empty[Match]

    def getMatchesView: List[MatchView] = matches.map(_.serialize)
    def getMatchViewById(matchId: String): Option[MatchView] = matches.find(_.id == matchId).map(_.serialize)
    def getMatchById(matchId: String): Option[Match] = matches.find(_.id == matchId)
    def create(wannaBeMatch: WannaBeMatch) = {
      def eventsFlowFor(boards: Boards) = {
        val tickingSource: Source[Tick, Cancellable] = Source.tick(
          initialDelay = 1 seconds,
          interval = 1 seconds,
          tick = Tick()
        )

        val startMatchSource: Source[StartMatch, NotUsed] = Source.single(StartMatch())

        implicit val timeout = Timeout(3 seconds)
        val (matchSink, matchSource) = MergeHub.source[MatchEvent]
          .merge(startMatchSource)
          .merge(tickingSource)
          .ask[Boards](system.actorOf(MatchActor.props(boards)))
          .map(Json.toJson(_))
          .toMat(BroadcastHub.sink[JsValue])(Keep.both)
          .run

        Flow.fromSinkAndSource(matchSink, matchSource)
      }

      val boards = wannaBeMatch.players.map(_ -> GameEngine(wannaBeMatch.size, RandomStoneFactory)).toMap
      val newMatch = Match(
        id = "match_" + UUID.randomUUID,
        label = wannaBeMatch.label,
        boards = boards,
        events = eventsFlowFor(boards)
      )

      matches = matches :+ newMatch

      newMatch.id
    }
  }
}
