package com.orangeade.tetris.server.controller

import java.util.UUID
import javax.inject._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.libs.json._
import play.api.libs.streams.ActorFlow

import akka.actor._
import akka.event.Logging
import akka.stream.Materializer

import com.orangeade.tetris.server.actor._
import com.orangeade.tetris.server.model.match_module._

@Singleton
class MatchController @Inject()(
  implicit system: ActorSystem,
  authenticatedAction: AuthenticatedAction,
  cc: ControllerComponents,
  implicit val executionContext: ExecutionContext,
  matchDAO: MatchDAO,
  materializer: Materializer,
) extends AbstractController(cc) {
  private val logger = Logger(getClass)

  /*def authenticate = Action.async(parse.json[UserToAuthenticate]) { implicit request =>
    Future {
      val userToAuthenticate = request.body
      userDAO.authenticate(userToAuthenticate) match {
        case Left(_) => Unauthorized
        case Right(token) => Ok(Json.toJson(token))
      }
    }
  }*/

  def getMatches = authenticatedAction.async { implicit request: Request[AnyContent] =>
    Future(Ok(Json.toJson(matchDAO.getMatchesView)))
  }

  def getMatchById(matchId: String) = authenticatedAction.async { implicit request =>
    Future(Ok(Json.toJson(matchDAO.getMatchViewById(matchId))))
  }

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[InEvent, JsValue]
  def matchSocket(matchId: String) = WebSocket.acceptOrResult[InEvent, JsValue] { request =>
    matchDAO.getMatchById(matchId).map { m =>
      Future.successful(m.events).map { flow =>
        Right(flow)
      }.recover {
        case e: Exception => {
          val msg = "Cannot create websocket"
          logger.error(msg, e)
          val result = InternalServerError(msg)
          Left(result)
        }
      }
    }.getOrElse(Future.successful(Left(BadRequest)))
  }

  /*def patchUserById(userId: UUID) = authenticatedAction.async(parse.json[User]) { implicit request =>
    Future {
      val user = request.body
      userDAO.patchUser(user) match {
        case Left(_) => InternalServerError
        case Right(_) => NoContent
      }
    }
  }
*/

  def create = authenticatedAction.async(parse.json[WannaBeMatch]) { implicit request =>
    Future {
      val m = request.body
      matchDAO.create(m) match {
        case Left(e) => InternalServerError("All playersId don't exist.")
        case Right(matchId) => Created(Json.toJson(matchId))
      }
    }
  }
}
