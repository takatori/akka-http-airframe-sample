package com.example

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.{ Matchers, WordSpecLike }
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class HelloSlickSpec extends WordSpecLike with Matchers with ScalaFutures {

  import HelloSlick._

  implicit val executor: ExecutionContext = ExecutionContext.global

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(200, Millis))

  val db = Database.forConfig("db")
  val helloSlick = HelloSlick(db)

  "hello slick" should {

    "crud!!" in {
      val created = helloSlick.create(Hello(0, "ME")).futureValue
      created.id shouldBe 1

      val findAll = helloSlick.findAll().futureValue
      val findById = helloSlick.findById(created.id).futureValue
      findAll.size shouldBe 1
      findById.nonEmpty shouldBe true
      findById.foreach(a => a.id shouldBe created.id)

      val updated = helloSlick.update(Hello(created.id, "ME2")).futureValue
      updated shouldBe 1

      val deleted = helloSlick.delete(created.id).futureValue
      deleted shouldBe 1
    }
  }

}
