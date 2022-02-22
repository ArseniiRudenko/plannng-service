package work.arudenko.kanban.backend.api

import akka.actor.ActorSystem
import akka.http.javadsl.model.headers.Authorization
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethods, HttpRequest, StatusCode, StatusCodes}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import work.arudenko.kanban.backend.model.User
import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

class UserApiSpec extends AnyFlatSpec with should.Matchers {

  implicit val actorSystem: ActorSystem = ActorSystem("test")
  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher


  "User api" should "return token as an answer to login request" in {
    val res =  Await.result(login,Duration.Inf)
      println(res._2.data.utf8String)
      res._1 shouldBe StatusCodes.OK
  }

  it should "return user info for request from logged in user" in {
    val userRes= login.flatMap(loginResult=> {
      val userRequest = HttpRequest(
        method = HttpMethods.GET,
        uri = "http://localhost:9000/user/me/",
        headers = Authorization.oauth2(loginResult._2.data.utf8String) :: Nil
      )
      Http().singleRequest(userRequest).flatMap(res=>{
        res.entity.toStrict(FiniteDuration.apply(5,TimeUnit.SECONDS)).map((res.status,_))
      })
    })
    val res= Await.result(userRes,Duration.Inf)
    println(res._2.data.utf8String)
    res._1 shouldBe StatusCodes.OK
  }


  it should "return user info for other user if requester is admin" in {
    val userRes= login.flatMap(loginResult=> {
      val userRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = "http://localhost:9000/user/other",
        headers = Authorization.oauth2(loginResult._2.data.utf8String) :: Nil,
        entity = "вася@правительство.рф"
      )
      Http().singleRequest(userRequest).flatMap(res=>{
        res.entity.toStrict(FiniteDuration.apply(5,TimeUnit.SECONDS)).map((res.status,_))
      })
    })
    val res= Await.result(userRes,Duration.Inf)
    println(res._2.data.utf8String)
    res._1 shouldBe StatusCodes.OK
  }



  lazy val login: Future[(StatusCode, HttpEntity.Strict)] = {
    val  loginRequest=HttpRequest(
      method = HttpMethods.POST,
      uri = "http://localhost:9000/user/login",
      entity = HttpEntity(ContentTypes.`application/json`, "{ \"email\":\"arsenii@test\", \"password\":\"test\"}")
    )
    Http().singleRequest(loginRequest).flatMap(res=>{
      res.entity.toStrict(FiniteDuration.apply(5,TimeUnit.SECONDS)).map((res.status,_))
    })

  }

}
