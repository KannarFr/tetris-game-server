package com.orangeade.tetris.server.model

import java.util.UUID
import javax.inject._

import scala.util.{Try, Success, Failure}

import play.api.db._
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

import com.orangeade.tetris.server.model.pg.pg_entity_module._
import com.orangeade.tetris.server.model.pg.AnormType._

object player_module {
  case class Player(
    id: String,
    nickname: String,
    email: String,
    password: String
  ) {
    def toView = PlayerView(id, nickname)
  }
  implicit val playerFormat = Json.format[Player]

  case class PlayerView(
    id: String,
    nickname: String
  )
  implicit val playerViewFormat = Json.format[PlayerView]

  case class WannaBePlayer(
    nickname: String,
    email: String,
    password: String
  )
  implicit val wannaBePlayerFormat = Json.format[WannaBePlayer]

  object Player {
    implicit val playerPgEntity: PgEntity[Player] = new PgEntity[Player] {
      val tableName = "player"

      val columns = List(
        PgField("player_id"), PgField("nickname"), PgField("email"), PgField("password")
      )

      def parser(prefix: String): RowParser[Player] = {
        get[String]("player_id") ~
        get[String]("nickname") ~
        get[String]("email") ~
        get[String]("password") map {
          case (id ~ nickname ~ email ~ password) =>
            Player(id, nickname, email, password)
        }
      }
    }
  }

  sealed abstract class PlayerError
  case object PlayerAlreadyPresent extends PlayerError
  case object UnhandledException extends PlayerError

  class PlayerDAO @Inject()(
    db: Database
  ) {
    def getPlayersByIds(playersId: List[String]): List[Player] = db.withConnection { implicit c =>
      SQL(selectSQL[Player] + " WHERE player_id IN (" + playersId.mkString(",") + ")") as (parser[Player]().*)
    }

    def getPlayerById(playerId: String): Option[Player] = db.withConnection { implicit c =>
      SQL(selectSQL[Player] + " WHERE player_id LIKE '${playerId}' ") as (parser[Player]().singleOpt)
    }

    def create(wannaBePlayer: WannaBePlayer): Either[PlayerError, Unit] = db.withConnection { implicit c =>
      Try {
        SQL(insertSQL[Player]).on(
          'player_id -> ("player_" + UUID.randomUUID.toString),
          'nickname -> wannaBePlayer.nickname,
          'email -> wannaBePlayer.email,
          'password -> wannaBePlayer.password
        ).execute
        ()
      } match {
        case Failure(e: org.postgresql.util.PSQLException) => {
          if(e.getSQLState == "23505") {
            Left(PlayerAlreadyPresent)
          } else {
            e.printStackTrace
            Left(UnhandledException)
          }
        }
        case Failure(e) => {
          e.printStackTrace
          Left(UnhandledException)
        }
        case Success(s) => Right(s)
      }
    }
  }
}
