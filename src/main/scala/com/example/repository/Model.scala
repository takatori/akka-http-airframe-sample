package com.example.repository

import slick.jdbc.H2Profile.api._

object Model {

  final case class Todo(id: Int, body: String)

  class TodoTable(tag: Tag) extends Table[Todo](tag, "todo") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def body = column[String]("body")

    override def * = (id, body) <> (Todo.tupled, Todo.unapply)
  }

}
