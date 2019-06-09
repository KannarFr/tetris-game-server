package com.orangeade.tetris.server.model

import java.util.UUID
import javax.inject._

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

  object Player {
    implicit val playerPgEntity: PgEntity[Player] = new PgEntity[Player] {
      val tableName = "player"

      val columns = List(
        PgField("playerId"), PgField("nickname"), PgField("email"), PgField("password")
      )

      def parser(prefix: String): RowParser[Player] = {
        get[String]("playerId") ~
        get[String]("nickname") ~
        get[String]("email") ~
        get[String]("password") map {
          case (id ~ nickname ~ email ~ password) =>
            Player(id, nickname, email, password)
        }
      }
    }
  }

  class PlayerDAO @Inject()(
    db: Database
  ) {
    def getPlayersByIds(playersId: List[String]): List[Player] = db.withConnection { implicit c =>
      SQL(selectSQL[Player] + " WHERE playerId IN (" + playersId.mkString(",") + ")") as (parser[Player]().*)
    }
  }
}
