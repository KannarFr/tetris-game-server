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

  type Boards = Map[PlayerView, GameEngine]

  final case class Match(
    id: String,
    boards: Boards,
    events: Flow[MatchEvent, JsValue, _]
  ) {
    def serialize = MatchView(id, boards)
  }

  final case class MatchView(
    id: String,
    boards: Boards,
  )
  implicit val matchViewWrites = Json.writes[MatchView]

  final case class WannaBeMatch(
    playersId: List[String],
    size: Size
  )
  implicit val wannaBeMatchReads = Json.reads[WannaBeMatch]

  sealed abstract class MatchError
  case object AllPlayersIdDontExist extends MatchError

  class MatchDAO @Inject()(
    implicit system: ActorSystem,
    implicit val ec: ExecutionContext,
    materializer: Materializer,
    playerDAO: PlayerDAO,
  ) {
    // matches are stored in RAM, so we only keep running matches
    private var matches = List.empty[Match]

    def getMatchesView: List[MatchView] = matches.map(_.serialize)
    def getMatchViewById(matchId: String): Option[MatchView] = matches.find(_.id == matchId).map(_.serialize)
    def getMatchById(matchId: String): Option[Match] = matches.find(_.id == matchId)
    def create(wannaBeMatch: WannaBeMatch): Either[MatchError, String] = {
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

      val playersView = playerDAO.getPlayersByIds(wannaBeMatch.playersId).map(_.toView)

      if (playersView.size == wannaBeMatch.playersId.size) {
        val boards = playersView.map(_ -> GameEngine(wannaBeMatch.size, RandomStoneFactory)).toMap
        val newMatch = Match(
          id = "match_" + UUID.randomUUID,
          boards = boards,
          events = eventsFlowFor(boards)
        )

        matches = matches :+ newMatch

        Right(newMatch.id)
      } else {
        Left(AllPlayersIdDontExist)
      }
    }
  }
}
