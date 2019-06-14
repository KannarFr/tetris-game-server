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
import com.orangeade.tetris.server.model.player_module.PlayerView

trait MatchEvent
case class InEvent(playerId: String, action: String) extends MatchEvent
case class Tick() extends MatchEvent
case class StartMatch() extends MatchEvent
case class StopMatch() extends MatchEvent

object MatchActor {
  def props(boards: Map[PlayerView, GameEngine]): Props = Props(new MatchActor(boards))
}
class MatchActor(boards: Map[PlayerView, GameEngine]) extends Actor {
  private val logger = Logger(getClass)

  def receive = {
    case _: StartMatch => {
      logger.debug(s"start received at ${ZonedDateTime.now}")
      sender ! boards
    }
    case _: Tick => {
      //logger.debug(s"tick received for yo at ${ZonedDateTime.now}")
      boards.values.map { gameEngine =>
        if (gameEngine.isGameRunning) gameEngine.moveDown
      }
      sender ! boards
    }
    case event: InEvent => {
      boards
        .keys
        .filter(_.id == event.playerId)
        .map(boards.get)
        .map { gameEngineOpt =>
          gameEngineOpt.map { gameEngine =>
            if (gameEngine.isGameRunning) {
              //logger.debug(s"Let's ${event.action} for ${event.playerId}.")
              event.action match {
                case "moveLeft" => gameEngine.moveLeft
                case "moveRight" => gameEngine.moveRight
                case "rotateLeft" => gameEngine.rotateLeft
                case "rotateRight" => gameEngine.rotateRight
              }
            }/* else {
              logger.debug("fini bro teste pas")
            }*/
          }
        }

      sender ! boards
    }
    case x: Any => {
      logger.debug("on sait pas" + x.toString)
    }
  }
}
