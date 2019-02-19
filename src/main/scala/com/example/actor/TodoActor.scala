package com.example.actor

import akka.actor.{ Actor, ActorLogging, Props }
import com.example.repository.TodoRepository
import scalaz.{ EitherT, \/ }

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.std.scalaFuture.futureInstance

object TodoActor {

  def props(todoRepository: TodoRepository) = Props(new TodoActor(todoRepository))

  // 受信系
  sealed trait Command
  final case object FindAllCommand extends Command
  final case class FindByIdCommand(id: Int) extends Command
  final case class CreateCommand(body: String) extends Command
  final case class UpdateCommand(id: Int, body: String) extends Command
  final case class DeleteCommand(id: Int) extends Command

  // 返信系
  sealed trait Reply
  final case class TodoReply(id: Int, body: String) extends Reply
  final case class CreatedReply(id: Int) extends Reply
  final case object UpdatedReply extends Reply
  final case object DeletedReply extends Reply

}

// TodoRepositoryをインジェクション
class TodoActor(todoRepository: TodoRepository)
  extends Actor with ActorLogging with EitherPipeToSupport {

  import TodoActor._
  import com.example.repository.Model._

  implicit val executor: ExecutionContext = context.dispatcher

  // Future[\/[A,B]] -> EitherT[Future, A, B]
  // モナドトランスフォーマー
  // enrich my library
  implicit class RichFutureEither[A, B](self: Future[\/[A, B]]) {
    def toEitherT: EitherT[Future, A, B] = EitherT[Future, A, B](self)
  }

  private def findAll() =
    for {
      todos <- todoRepository.findAll().toEitherT
    } yield todos.map(t => TodoReply(t.id, t.body))

  private def findById(cmd: FindByIdCommand) =
    for {
      todo <- todoRepository.findById(cmd.id).toEitherT
    } yield todo.map(t => TodoReply(t.id, t.body))

  private def create(cmd: CreateCommand) =
    for {
      createdId <- todoRepository.create(Todo(0, cmd.body)).toEitherT
    } yield CreatedReply(createdId)

  private def update(cmd: UpdateCommand) =
    for {
      _ <- todoRepository.update(Todo(cmd.id, cmd.body)).toEitherT
    } yield UpdatedReply

  private def delete(cmd: DeleteCommand) =
    for {
      _ <- todoRepository.delete(cmd.id).toEitherT
    } yield DeletedReply

  override def preStart(): Unit = log.info("starting todo actor.")
  override def postStop(): Unit = log.info("stopping todo actor.")

  override def receive: Receive = {
    case FindAllCommand => eitherPipe(findAll().run) to sender()
    case cmd: FindByIdCommand => eitherPipe(findById(cmd).run) to sender()
    case cmd: CreateCommand => eitherPipe(create(cmd).run) to sender()
    case cmd: UpdateCommand => eitherPipe(update(cmd).run) to sender()
    case cmd: DeleteCommand => eitherPipe(delete(cmd).run) to sender()
    case unknown => log.error(s"receive unknown type. type: ${unknown.getClass.getName}")
  }
}
