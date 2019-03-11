package com.example.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, concat, entity, onSuccess, pathEnd, pathPrefix, rejectEmptyResponse}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.example.JsonSupport
import com.example.actor.{User, UserActorJsonSerializer, Users}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask

import scala.concurrent.Future
import scala.util.control.NonFatal

object UserRoutes {
  def apply(userActor: ActorRef)(implicit timeout: Timeout): UserRoutes =
    new UserRoutes(userActor)
}

class UserRoutes(userActor: ActorRef)(implicit timeout: Timeout)
  extends SprayJsonSupport with UserActorJsonSerializer {

  import com.example.actor.UserRegistryActor._


  implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case NonFatal(ex) =>
      extractLog { implicit log =>
        extractUri { uri =>
          log.error(s"raised error!! uri: $uri, reason: ${ex.getMessage}")
          complete(StatusCodes.InternalServerError -> s"raised error!! uri: $uri, reason: ${ex.getMessage}")
        }
      }
  }


  lazy val userRoutes: Route = handleExceptions(exceptionHandler) {
    extractLog { implicit log =>
      pathPrefix("users") {
        pathEndOrSingleSlash {
          get {
            onSuccess((userActor ? GetUsers).mapTo[Users]) { res =>
             complete(StatusCodes.OK -> res)
            }
          }
          /*~
            post {
              entity(as[User]) { user =>
                val userCreated: Future[ActionPerformed] =
                  (userActor ? CreateUser(user)).mapTo[ActionPerformed]
                onSuccess(userCreated) { performed =>
                  log.info("Created user [{}]: {}", user.name, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
        } ~
          path(Segment) { name =>
            get {
              val maybeUser: Future[Option[User]] =
                (userActor ? GetUser(name)).mapTo[Option[User]]
              rejectEmptyResponse {
                complete(maybeUser)
              }
            } ~
              delete {
                val userDeleted: Future[ActionPerformed] =
                  (userActor ? DeleteUser(name)).mapTo[ActionPerformed]
                onSuccess(userDeleted) { performed =>
                  log.info("Deleted user [{}]: {}", name, performed.description)
                  complete((StatusCodes.OK, performed))
                }
              }*/
          }
      }
    }
  }
}
