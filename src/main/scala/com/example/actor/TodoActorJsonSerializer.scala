package com.example.actor

import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait TodoActorJsonSerializer extends DefaultJsonProtocol {

  import TodoActor._

  implicit val findByIdCommand: RootJsonFormat[FindByIdCommand] = jsonFormat1(FindByIdCommand)
  implicit val createCommand: RootJsonFormat[CreateCommand] = jsonFormat1(CreateCommand)
  implicit val updateCommand: RootJsonFormat[UpdateCommand] = jsonFormat2(UpdateCommand)
  implicit val deleteCommand: RootJsonFormat[DeleteCommand] = jsonFormat1(DeleteCommand)

  implicit val todoReplyFormat: RootJsonFormat[TodoReply] = jsonFormat2(TodoReply)
  implicit val createdReply: RootJsonFormat[CreatedReply] = jsonFormat1(CreatedReply)
}
