package work.arudenko.kanban.backend.api

import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, Cancellable}
import akka.http.javadsl.model.headers.Authorization
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import work.arudenko.kanban.backend.model.User

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}

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

  it should "handle load on simple non db requests" in {

    val loginResult =  Await.result(login,Duration.Inf)
    val source2: Source[(HttpRequest,UUID),NotUsed] =
      Source(Range(0,100000).map(i=>
        (HttpRequest(
          method = HttpMethods.GET,
          uri = "http://localhost:9000/user/me/",
          headers = Authorization.oauth2(loginResult._2.data.utf8String) :: Nil
        ),UUID.randomUUID())))

      val flow = Http().cachedHostConnectionPool[UUID]("localhost",9000)

      val sink= Sink.foreach[(Try[HttpResponse],UUID)](res=>{
        res._1.map(successResult => {
          successResult.status shouldBe StatusCodes.OK
          successResult.entity.toStrict(FiniteDuration.apply(5, TimeUnit.SECONDS))
            .onComplete {
              case Failure(exception) => fail(exception)
              case Success(value) =>
                println(s"request ${res._2} finished with value: ${value.data.utf8String}")
            }
        }
        ) match {
          case Failure(exception) => fail(exception)
          case Success(value) => println("finished sucessfully")
        }
      })
      Await.result(source2.via(flow).runWith(sink),Duration.Inf)
  }

  it should "handle load on db requests" in {

    val loginResult =  Await.result(login,Duration.Inf)
    val source2: Source[(HttpRequest,UUID),NotUsed] =
      Source(Range(0,100000).map(_=>
        (HttpRequest(
          method = HttpMethods.POST,
          uri = "http://localhost:9000/admin/user",
          headers = Authorization.oauth2(loginResult._2.data.utf8String) :: Nil,
          entity = "{\"email\":\"вася@правительство.рф\"}"
        ),UUID.randomUUID())))

    val flow = Http().cachedHostConnectionPool[UUID]("localhost",9000)

    val sink= Sink.foreach[(Try[HttpResponse],UUID)](res=>{
      res._1.map(successResult => {
        successResult.status shouldBe StatusCodes.OK
        successResult.entity.toStrict(FiniteDuration.apply(5, TimeUnit.SECONDS))
          .onComplete {
            case Failure(exception) => fail(exception)
            case Success(value) =>
              println(s"request ${res._2} finished with value: ${value.data.utf8String}")
          }
      }
      ) match {
        case Failure(exception) => fail(exception)
        case Success(value) => println("finished sucessfully")
      }
    })
    Await.result(source2.via(flow).runWith(sink),Duration.Inf)
  }

  it should "return user info for other user if requester is admin" in {
    val userRes= login.flatMap(loginResult=> {
      val userRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = "http://localhost:9000/admin/user",
        headers = Authorization.oauth2(loginResult._2.data.utf8String) :: Nil,
        entity = "{\"email\":\"вася@правительство.рф\"}"
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
