package work.arudenko.kanban.backend.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import com.redis.api.StringApi.Always
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.UserApiService
import work.arudenko.kanban.backend.model.{GeneralError, User, UserCreationInfo, UserInfo, UserUpdateInfo}
import work.arudenko.kanban.backend.services.EmailService
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

class UserApiServiceImpl(implicit actorSystem: ActorSystem) extends UserApiService with LazyLogging with AuthenticatedRoute {

  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  val emailVerificationPrefix = "e"
  val emailVerificationDeadline: FiniteDuration = Duration(3,TimeUnit.DAYS)

  val passwordResetPrefix = "p"
  val passwordResetDeadline: FiniteDuration = Duration(12,TimeUnit.HOURS)

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUser(user: UserCreationInfo)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = {
    val userWitPass: UserCreationInfo = HashPassword(user)
    val id = User.signUp(userWitPass)
    id match {
      case Some(value) => scala.concurrent.Future {
        val finalUser =
          new User(
            value.toInt,
            userWitPass.firstName,
            userWitPass.lastName,
            Some(userWitPass.email),
            Some(userWitPass.password),
            userWitPass.phone,
            false,
            false
          )
        val emailVerificationToken=generateSessionToken(finalUser,emailVerificationDeadline,Some(emailVerificationPrefix))
        EmailService.sendActivaltionEmail(finalUser,emailVerificationToken)
      }.onComplete {
        case Failure(exception) => logger.error("failed sending sign up email", exception)
        case Success(value) => logger.trace(s"success sending sign up email")
      }
      User200
      case None => User400(GeneralError("failed creating user,info is wrong or user already exists"))
    }


  }

  private def HashPassword(user: UserCreationInfo): UserCreationInfo = {
    val salt: Array[Byte] = generateSalt
    val passHash = generateArgon2id(user.password, salt).toBase64
    val userWitPass = user.copy(password = s"$passHash:${salt.toBase64}")
    userWitPass
  }

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

  import work.arudenko.kanban.backend.orm.RedisContext._
  import com.redis.serialization._
  import com.redis.serialization.Parse.Implicits._
  import boopickle.Default._

  private def getUserFromToken(id:String,oneTime:Boolean=true):Option[User] = {
    redis.withClient {
      client =>{
        val result = client.get[User](id)
        if(oneTime) result.foreach(_=>client.del(id))
        result
      }
    }
  }

  private def generateSessionToken(user: User, expires:FiniteDuration = authDuration, prefix:Option[String]=None): String = {
    val sessionToken = prefix match {
      case Some(value) => s"$value:${generateSalt.toBase64}"
      case None => generateSalt.toBase64
    }
    scala.concurrent.Future {
      redis.withClient {
        client => client.set(sessionToken, user, Always, expires)(userSerializer)
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
        if(!user.enabled) return User400(GeneralError("user disabled"))
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
          case None => User400(GeneralError("user disabled"))
        }
      case None => User400(GeneralError("wrong login or password"))
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
  override def createUsersWithArrayInput(user: Seq[UserInfo])(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global", authenticator) {
      auth =>
        if(auth.user.admin)
          User.createUsers(user) match {
            case Some(value) => value match {
              case v if v.length == user.length => User200
              case v => User400(new GeneralError(s"created $v users out of ${user.length}"))
            }
            case None => User400(new GeneralError(s"failed creating users"))
          }
        else
          User403
    }
  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def updateUser(user: UserUpdateInfo)(implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global", authenticator) {
      auth =>
        if(auth.user.admin) {
          ???
        }else if(???) {
          ???
        }else
          User403
    }

  override def resetPassword(resetToken: String, newPassword: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???


  override def activateAccount(emailToken: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = {
    getUserFromToken(emailToken) match {
      case Some(value) => User.emailActivateAccount(value.id) match {
        case 1 => User200
        case 0 => User404
        case e => logger.error(s"returned number of records $e when trying to activate user $value by email token $emailToken");User500
      }
      case None => User404
    }
  }

  override def requestPasswordReset(email: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = {
    scala.concurrent.Future {
      User.getLoginUser(email) match {
        case Some(value) =>
          val pwResetToken = generateSessionToken(value, passwordResetDeadline, Some(passwordResetPrefix))
          EmailService.sendPasswordResetEmail(value, pwResetToken)
        case None => logger.info(s"requested reset for non existing email $email")
      }
    }.onComplete {
      case Failure(exception) => logger.error("failed sending password reset", exception)
      case Success(_) => logger.trace(s"successfully sent password reset email")
    }
    User200
  }
}
