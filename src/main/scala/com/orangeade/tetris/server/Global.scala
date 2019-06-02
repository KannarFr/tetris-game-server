package com.orangeade.tetris.server

import javax.inject._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import play.api._
import play.api.libs.concurrent.AkkaGuiceSupport

import akka.actor.{ActorSystem, ActorRef}
import com.google.inject.AbstractModule

import com.orangeade.tetris.server.actor._

@Singleton
class Global @Inject() (
  actorSystem: ActorSystem,
  app: Application,
)(implicit ec: ExecutionContext) {
  private val logger = Logger(getClass)
  logger.info(s"STARTING DAEMON...")
  logger.info(s"STARTED.")
}

class GlobalModule extends AbstractModule with AkkaGuiceSupport {
  override def configure = {
    bind(classOf[Global]).asEagerSingleton
  }
}
