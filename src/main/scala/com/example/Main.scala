package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.settings.ServerSettings
import wvlet.airframe._

object Main {

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

  }
}
