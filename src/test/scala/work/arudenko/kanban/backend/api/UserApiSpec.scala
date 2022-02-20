package work.arudenko.kanban.backend.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, StatusCode, StatusCodes}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import work.arudenko.kanban.backend.model.User

import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

class UserApiSpec extends AnyFlatSpec with should.Matchers {




  "User api" should "return token as an answer to login request" in {
    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
    val  loginRequest=HttpRequest(
      method = HttpMethods.POST,
      uri = "http://localhost:9000/user/login",
      entity = HttpEntity(ContentTypes.`application/json`, "{ \"email\":\"arsenii@test\", \"password\":\"test\"}")
    )
    val res= Await.result(Http().singleRequest(loginRequest).flatMap(res=>{
      res.entity.toStrict(FiniteDuration.apply(5,TimeUnit.SECONDS)).map((res.status,_))
    }),Duration.Inf)
      println(res._2.data.utf8String)
      res._1 shouldBe StatusCodes.OK
  }

}
