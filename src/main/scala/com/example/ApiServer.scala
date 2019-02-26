package com.example

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.example.routes.UserRoutes
import wvlet.airframe._

trait ApiServer {

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = bind[ActorSystem]
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  // from the UserRoutes trait
  lazy val routes: Route = bind[UserRoutes].userRoutes

  def start(host: String, port: Int, settings: ServerSettings): Future[ServerBinding] = {

    val bindingFuture = Http().bindAndHandle(handler = routes, interface = host, port = port, settings = settings)

    bindingFuture.onComplete {
      case Success(binding) =>
        system.log.info(s"Server online at http://${binding.localAddress.getHostName}:${binding.localAddress.getPort}/")
      case Failure(ex) =>
        system.log.error(ex, "occurred error")
    }

    sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete { _ =>
          materializer.shutdown()
          system.terminate()
        }
    }

    bindingFuture
  }
}
