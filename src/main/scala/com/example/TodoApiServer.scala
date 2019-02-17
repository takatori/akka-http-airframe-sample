package com.example

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ HttpApp, Route }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object TodoApiServer {
  def apply(todoRoutes: TodoRoutes): TodoApiServer = new TodoApiServer(todoRoutes)
}

class TodoApiServer(todoRoutes: TodoRoutes) extends HttpApp {

  override protected val routes: Route = todoRoutes.routes

  override protected def postServerShutdown(attempt: Try[Done], system: ActorSystem): Unit = {
    super.postServerShutdown(attempt, system)
    system.terminate()
    Await.result(system.whenTerminated, 30.seconds)
  }
}
