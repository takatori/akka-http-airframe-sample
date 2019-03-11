package com.example

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.settings.ServerSettings
import wvlet.airframe._

import scala.concurrent.duration._
import akka.util.Timeout
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext
import com.example.repository.{TodoRepository, TodoRepositoryImpl}
import com.example.routes.{TodoApiServer, TodoRoutes}

object Main {

  /*
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("actor")

    val design = newDesign
      .bind[ActorSystem].toInstance(system)
      .bind[UserRoutes].toSingleton
      .bind[ApiServer].toSingleton
      .bind[ActorRef].toInstance(system.actorOf(UserRegistryActor.props, "userRegistryActor"))

    design.withSession { session =>
      val system = session.build[ActorSystem]
      session.build[ApiServer].start("localhost", 8080, settings = ServerSettings(system))
    }
  }*/

  import com.example.actor._

  implicit val system: ActorSystem = ActorSystem("todo-api")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  // レポジトリ定義
  val db = Database.forConfig("todo-slick-db")
  val todoRepository: TodoRepository = TodoRepositoryImpl(db)

  // アクター定義
  val todoSupervisor = system.actorOf(TodoSupervisor.props(3, 30.seconds))
  todoSupervisor ! TodoSupervisor.RegistrationCommand(TodoActor.props(todoRepository))

  // ルート定義
  val todoRoutes = TodoRoutes(todoSupervisor)
  val todoApiServer = TodoApiServer(todoRoutes)

  todoApiServer.startServer("0.0.0.0", 8000, system)

}
