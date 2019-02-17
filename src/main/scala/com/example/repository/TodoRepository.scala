package com.example.repository

import scala.concurrent.Future
import scalaz.\/

trait TodoRepository {

  import Model._

  def findAll(): Future[\/[Throwable, Seq[Todo]]]

  def findById(id: Int): Future[\/[Throwable, Option[Todo]]]

  def create(data: Todo): Future[\/[Throwable, Int]]

  def update(data: Todo): Future[\/[Throwable, Int]]

  def delete(id: Int): Future[\/[Throwable, Int]]

}
