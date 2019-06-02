package com.orangeade.tetris.server.model

import java.util.UUID

import play.api.libs.json._

object player_module {
  case class Player(
    id: String
  )
  implicit val playerFormat = Json.format[Player]
}
