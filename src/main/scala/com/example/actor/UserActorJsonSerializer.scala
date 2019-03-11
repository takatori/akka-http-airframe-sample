package com.example.actor

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserActorJsonSerializer extends DefaultJsonProtocol {

  import UserRegistryActor._

  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
  implicit val usersFormat: RootJsonFormat[Users] = jsonFormat1(Users)

}
