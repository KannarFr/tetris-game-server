package com.orangeade.tetris.server.actor

import java.time.ZonedDateTime
import javax.inject._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }

import play.api.libs.json._
import play.api.Logger

import akka.actor._

import com.orangeade.tetris.game.GameEngine
import com.orangeade.tetris.server.model.match_module._
import com.orangeade.tetris.server.model.player_module.Player

trait MatchEvent
case class InEvent(playerId: String, action: String) extends MatchEvent
case class Tick() extends MatchEvent
case class StartMatch() extends MatchEvent
case class StopMatch() extends MatchEvent

object MatchActor {
  def props(boards: Map[Player, GameEngine]): Props = Props(new MatchActor(boards))
}
class MatchActor(boards: Map[Player, GameEngine]) extends Actor {
  private val logger = Logger(getClass)

  def receive = {
    case _: StartMatch => {
      logger.debug(s"start received at ${ZonedDateTime.now}")
      sender ! MatchView("yo", None, boards)
    }
    case _: Tick => {
      logger.debug(s"tick received for yo at ${ZonedDateTime.now}")
      boards.values.map { gameEngine =>
        if (gameEngine.isGameRunning) gameEngine.moveDown
      }
      val yo = MatchView("yo", None, boards)
      println(yo)
      sender ! yo
    }
    case x: InEvent => {
      logger.debug("mdr")
      sender ! "yo"
    }
    case x: Any => {
      logger.debug("on sait pas" + x.toString)
    }
  }
}
