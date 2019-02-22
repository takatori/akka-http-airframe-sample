package com.example

import akka.actor.{ ActorSystem, Status }
import akka.testkit.{ ImplicitSender, TestKit }
import com.example.actor.TodoActor
import com.example.repository.{ TodoRepository, TodoRepositoryImpl }
import org.scalatest.{ MustMatchers, WordSpec, WordSpecLike }
import slick.jdbc.H2Profile.api._

class TodoActorSpec extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

  "A TodoActor" must {

    import com.example.actor.TodoActor._

    // レポジトリ定義
    val db = Database.forConfig("todo-slick-db")
    val todoRepository: TodoRepository = TodoRepositoryImpl(db)

    // アクター定義
    val props = TodoActor.props(todoRepository)
    val todoActor = system.actorOf(props, "todoActor")

    "create todo" in {
      todoActor ! CreateCommand("test")
      expectMsg(CreatedReply(1))
    }

    "send todos" in {
      todoActor ! FindAllCommand
      expectMsg(Seq(TodoReply(1, "test")))
    }
  }

}
