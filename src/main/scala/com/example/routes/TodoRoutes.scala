package com.example.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.pattern.ask
import akka.util.Timeout
import com.example.actor.TodoActorJsonSerializer

import scala.util.control.NonFatal

object TodoRoutes {
  def apply(todoActor: ActorRef)(implicit timeout: Timeout): TodoRoutes =
    new TodoRoutes(todoActor)
}

class TodoRoutes(todoActor: ActorRef)(implicit timeout: Timeout)
  extends SprayJsonSupport with TodoActorJsonSerializer {

  import com.example.actor.TodoActor._

  implicit def todoExceptionHandler: ExceptionHandler = ExceptionHandler {
    case NonFatal(ex) =>
      extractLog { implicit log =>
        extractUri { uri =>
          log.error(s"raised error!! uri: $uri, reason: ${ex.getMessage}")
          complete(StatusCodes.InternalServerError -> s"raised error!! uri: $uri, reason: ${ex.getMessage}")
        }
      }
  }

  val routes: Route = handleExceptions(todoExceptionHandler) {
    extractLog { implicit log =>
      extractUri { uri =>
        extractMethod { method =>
          log.info(s"call api. method: ${method.value}, uri: $uri")
          pathPrefix("todos") {
            pathEndOrSingleSlash {
              get {
                onSuccess((todoActor ? FindAllCommand).mapTo[Seq[TodoReply]]) { res =>
                  complete(StatusCodes.OK -> res)
                }
              } ~
                post {
                  entity(as[CreateCommand]) { req =>
                    onSuccess((todoActor ? req).mapTo[CreatedReply]) { res =>
                      complete(StatusCodes.OK -> res)
                    }
                  }
                }
            } ~
              path(IntNumber) { id =>
                get {
                  onSuccess((todoActor ? FindByIdCommand(id)).mapTo[Option[TodoReply]]) {
                    case Some(todoReply) => complete(StatusCodes.OK -> todoReply)
                    case None => complete(StatusCodes.NotFound)
                  }
                } ~
                  put {
                    entity(as[UpdateCommand]) { req =>
                      onSuccess(todoActor ? req) { _ => complete(StatusCodes.NoContent)
                      }
                    }
                  } ~
                  delete {
                    onSuccess(todoActor ? DeleteCommand(id)) { _ =>
                      complete(StatusCodes.NoContent)
                    }
                  }
              }
          }
        }
      }
    }
  }
}