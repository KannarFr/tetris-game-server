package com.orangeade.tetris.server

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

object Server extends App {
  implicit val system: ActorSystem = ActorSystem("daemon")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val routes: Flow[HttpRequest, HttpResponse, _] =
    Flow[HttpRequest]
      .map { request =>
        request match {
          case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
            HttpResponse(entity = HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              "<html><body>Hello world!</body></html>"
            ))
          }
          case r: HttpRequest => {
            r.discardEntityBytes()
            HttpResponse(404, entity = "Unknown resource!")
          }
        }
      }

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 9000)

  serverBinding.onComplete {
    case Success(bound) => {
      println(s"Server online at ${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    }
    case Failure(e) => {
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
    }
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
