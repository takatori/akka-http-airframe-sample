package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.routes.UserRoutes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import wvlet.airframe._

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with JsonSupport {

  private val testDesign = newDesign
    .bind[UserRoutes].toSingleton
    .bind[ActorSystem].toInstance(system)
    .bind[ActorRef].toInstance(system.actorOf(UserRegistryActor.props, "userRegistryActor"))

  testDesign.withSession { session =>

    val routes = session.build[UserRoutes].userRoutes

    "UserRoutes" should {

      "return no users if no present (GET /users)" in {

        // note that there's no need for the host part in the uri:
        val request = HttpRequest(uri = "/users")

        request ~> routes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===("""{"users":[]}""")
        }
      }

      //#testing-post
      "be able to add users (POST /users)" in {
        val user = User("Kapi", 42, "jp")
        val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

        // using the RequestBuilding DSL:
        val request = Post("/users").withEntity(userEntity)

        request ~> routes ~> check {
          status should ===(StatusCodes.Created)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and we know what message we're expecting back:
          entityAs[String] should ===("""{"description":"User Kapi created."}""")
        }
      }
      //#testing-post

      "be able to remove users (DELETE /users)" in {
        // user the RequestBuilding DSL provided by ScalatestRouteSpec:
        val request = Delete(uri = "/users/Kapi")

        request ~> routes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)

          // and no entries should be in the list:
          entityAs[String] should ===("""{"description":"User Kapi deleted."}""")
        }
      }
    }

  }
}