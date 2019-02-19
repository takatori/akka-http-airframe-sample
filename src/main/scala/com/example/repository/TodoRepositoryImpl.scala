package com.example.repository

import scalaz.\/
import scalaz.syntax.ToEitherOps
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal
import ExecutionContext.Implicits.global

object TodoRepositoryImpl {

  def apply(db: Database): TodoRepositoryImpl = new TodoRepositoryImpl(db)
}

// Databaseインスタンスをコンストラクタインジェクション
class TodoRepositoryImpl(db: Database) extends TodoRepository with ToEitherOps {

  import Model._

  lazy val todos = TableQuery[TodoTable]

  // Future[R]ではなくFuture[\/[Throwable, R]]を返すようにしたラッパー
  private def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[\/[Throwable, R]] =
    db.run(a).map(_.right[Throwable]).recover {
      case NonFatal(ex) => ex.left[R]
    }

  override def findAll(): Future[Throwable \/ Seq[Todo]] = run(todos.result)

  override def findById(id: Int): Future[Throwable \/ Option[Todo]] = {
    val q = for {
      r <- todos if r.id === id
    } yield r
    run(q.result.headOption)
  }

  override def create(data: Todo): concurrent.Future[Throwable \/ Int] =
    run(todos returning todos.map(_.id) += data)

  override def update(data: Todo): Future[Throwable \/ Int] = {
    val q = for {
      r <- todos if r.id === data.id
    } yield r.body
    run(q.update(data.body))
  }

  override def delete(id: Int): Future[Throwable \/ Int] = {
    val q = for {
      r <- todos if r.id === id
    } yield r
    run(q.delete)
  }
}

