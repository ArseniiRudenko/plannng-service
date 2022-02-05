package work.arudenko.kanban.backend.controller

import akka.actor.ActorSystem
import akka.http.javadsl.server.directives.SecurityDirectives.ProvidedCredentials
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import com.redis.api.StringApi.Always
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.TxBoundary.Future
import work.arudenko.kanban.backend.api.UserApiService
import work.arudenko.kanban.backend.model.{GeneralError, User, UserCreationInfo, UserInfo, UserUpdateInfo}

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class UserApiServiceImpl(actorSystem: ActorSystem) extends UserApiService with LazyLogging with AuthenticatedRoute {

  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUser(user: UserCreationInfo)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def deleteUser(username: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global", authenticator) {
      auth =>
        if(auth.user.admin || auth.user.email.contains(username))
          User.getId(username) match {
            case Some(value) => User.delete(value); User200
            case None => User404
          }
        else
          User403
    }
  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def getUserByName(username: String)(implicit toEntityMarshallerUser: ToEntityMarshaller[UserInfo], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global", authenticator) {
      _ =>
        User.getUser(username) match {
          case Some(value) => getUserByName200(UserInfo(value))
          case None => User404
        }
    }

  private def fakeCalculatingAndFuckOff(pw: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = {
    Credentials(Some(OAuth2BearerToken(pw)))
      .asInstanceOf[Credentials.Provided]
      .verify(null, pw => generateArgon2id(pw, "").toBase64)
    User400(GeneralError("wrong login or password"))
  }

  import work.arudenko.kanban.backend.orm.RedisContext._
  import com.redis.serialization._
  import com.redis.serialization.Parse.Implicits._
  import boopickle.Default._

  private def generateSessionToken(user: User): String = {
    val sessionToken = generateSalt.toBase64
    scala.concurrent.Future {
      redis.withClient {
        client => client.set(sessionToken, user, Always, Duration.create(4, TimeUnit.HOURS))(userSerializer)
      }
    }.onComplete {
      case Failure(exception) => logger.error("failed creating session token", exception)
      case Success(value) => logger.trace(s"created session token, result returned is $value")
    }
    sessionToken
  }

  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def loginUser(username: String, password: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    User.getLoginUser(username) match {
      case Some(user) =>
        val creds = Credentials(Some(OAuth2BearerToken(password))).asInstanceOf[Credentials.Provided]
        user.password match {
          case Some(storedPassword) => {
            val (secret, salt) = storedPassword.splitAt(storedPassword.lastIndexOf(":"))
            val result = creds.verify(secret, curPw => generateArgon2id(curPw, salt).toBase64)
            if (result) {
              loginUser200(generateSessionToken(user))
            } else {
              User400(GeneralError("wrong login or password"))
            }
          }
          case None => fakeCalculatingAndFuckOff(password)
        }
      case None => fakeCalculatingAndFuckOff(password)
    }

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def logoutUser()(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global", authenticator) {
      auth =>
        redis.withClient {
          client =>
            client.del(auth.token) match {
              case Some(value) if value == 1 => User200
              case Some(value) =>
                logger.warn(s"logout for user ${auth.user} and token ${auth.token} returned value $value, which is not expected")
                User200
              case None => User400(GeneralError("not logged in"))
            }
        }
    }

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUsersWithArrayInput(user: Seq[UserCreationInfo])(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global", authenticator) {
      auth =>
        if(auth.user.admin)
          ???
        else
          User403
    }
  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def updateUser(user: UserUpdateInfo)(implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???
}
