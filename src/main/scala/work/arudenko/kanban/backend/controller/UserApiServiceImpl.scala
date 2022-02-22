package work.arudenko.kanban.backend.controller

import akka.actor.ActorSystem
import com.redis.api.StringApi.Always
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.UserApiService
import work.arudenko.kanban.backend.model.{GeneralResult, NotAuthorized, NotFound, Result, SuccessEmpty, SuccessEntity, User, UserCreationInfo, UserInfo, UserUpdateInfo, WrongInput}
import work.arudenko.kanban.backend.serialization.binary.UserApiMarshallerImpl.{userParser, userSerializer}
import work.arudenko.kanban.backend.services.EmailService

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

class UserApiServiceImpl(implicit actorSystem: ActorSystem) extends UserApiService with LazyLogging{

  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  val emailVerificationPrefix = "e"
  val emailVerificationDeadline: FiniteDuration = Duration(3,TimeUnit.DAYS)

  val passwordResetPrefix = "p"
  val passwordResetDeadline: FiniteDuration = Duration(12,TimeUnit.HOURS)

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUser(user: UserCreationInfo):Result[UserInfo] = {
    val userWitPass: UserCreationInfo = user.copy(password = Auth.hashPassword(user.password))
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
        case Success(_) => logger.trace(s"success sending sign up email")
      }
        SuccessEmpty
      case None => WrongInput("failed creating user,info is wrong or user already exists")
    }
  }

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def deleteUser(username: String)(implicit auth: Auth):Result[User] =
    if(auth.user.admin || auth.user.email.contains(username))
      User.getId(username) match {
        case Some(value) => User.delete(value); SuccessEmpty
        case None => NotFound
      }
    else
      NotAuthorized

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def getUserByName(username: String)(implicit auth: Auth):Result[UserInfo] =
    User.getUser(username) match {
      case Some(value) => SuccessEntity(UserInfo(value))
      case None => NotFound
    }

  import work.arudenko.kanban.backend.orm.RedisContext._

  private def getUserFromToken(id:String,oneTime:Boolean=true):Option[User] = {
    redis.withClient {
      client =>{
        val result = client.get[User](id)(userSerializer,userParser)
        if(oneTime) result.foreach(_=>client.del(id))
        result
      }
    }
  }

  private def generateSessionToken(user: User, expires:FiniteDuration = Auth.authDuration, prefix:Option[String]=None): String = {
    val sessionToken = prefix match {
      case Some(value) => s"$value:${Auth.generateSalt.toBase64}"
      case None => Auth.generateSalt.toBase64
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
  override def loginUser(username: String, password: String):Result[String] =
    User.getLoginUser(username) match {
      case Some(user) =>
        if(!user.enabled) return WrongInput("user disabled")
        user.password match {
          case Some(storedPassword) =>
            if (Auth.verifyPassword(password, storedPassword)) {
              SuccessEntity(generateSessionToken(user))
            } else {
              logger.trace(s"failed password match for '$username' and '$password' with '$storedPassword'")
              WrongInput("wrong login or password")
            }
          case None => WrongInput("user disabled")
        }
      case None => NotFound
    }

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def logoutUser(auth: Auth):Result[User] =
        redis.withClient {
          client =>
            client.del(auth.token) match {
              case Some(value) if value == 1 => SuccessEmpty
              case Some(value) =>
                logger.warn(s"logout for user ${auth.user} and token ${auth.token} returned value $value, which is not expected")
                SuccessEmpty
              case None => WrongInput("not logged in")
            }
        }

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUsersWithArrayInput(user: Seq[UserInfo])(implicit auth: Auth):Result[User] =
        if(auth.user.admin)
          User.createUsers(user) match {
            case Some(value) => value match {
              case v if v.length == user.length => SuccessEmpty
              case v => WrongInput(s"created ${v.length} users out of ${user.length}")
            }
            case None => WrongInput(s"failed creating users")
          }
        else
          NotAuthorized

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def updateUser(user: UserUpdateInfo)(implicit auth: Auth):Result[User] = {
    val userWithPreparedValues = user.copy(newPassword = user.newPassword.map(Auth.hashPassword))
    User.getUser(user.email) match {
        case Some(oldUserValues) =>
          processUserUpdate(oldUserValues, userWithPreparedValues)
        case None => NotFound
    }
  }

  def processUserUpdate(oldSet:User,newSet:UserUpdateInfo):Result[User] =
    User.updateUser(oldSet,newSet) match {
      case Some(value) => value match {
        case 1=> SuccessEmpty
        case 0=> NotFound
        case e=>
          logger.error(s"update user returned unexpected value $e from update operation for user $oldSet and update set of $newSet")
          GeneralResult(500,"unexpected db error")
      }
      case None => WrongInput("incorrect values set for update")
    }

  override def resetPassword(resetToken: String, newPassword: String):Result[User] =
    getUserFromToken(resetToken) match {
      case Some(value) =>
        val hash = Auth.hashPassword(newPassword)
        User.setPassword(value.id, hash) match {
          case 1 => SuccessEmpty
          case e =>
            logger.error(s"reset password returned unexpected value $e from update operation for user $value and hash value of $hash")
            GeneralResult(500,"db error")
        }
      case None => NotFound
    }


  override def activateAccount(emailToken: String):Result[User] = {
    getUserFromToken(emailToken) match {
      case Some(value) => User.emailActivateAccount(value.id) match {
        case 1 => SuccessEmpty
        case 0 => NotFound
        case e =>
          logger.error(s"returned number of records $e when trying to activate user $value by email token $emailToken")
          GeneralResult(500,"db error")
      }
      case None => NotFound
    }
  }

  override def requestPasswordReset(email: String):Result[User]= {
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
    SuccessEmpty
  }

  override def getCurrentUser(auth: Auth): Result[UserInfo] =
    SuccessEntity(UserInfo(auth.user))


  override def deleteUser(auth: Auth): Result[User] = {
    User.delete(auth.user.id) match {
      case 0 => WrongInput("user already removed")
      case 1 => SuccessEmpty
      case p =>
        logger.error(s"returned $p value from request to remove user ${auth.user} by himself")
        GeneralResult(500,"db error")
    }
  }

  override def updateSelf(user: UserUpdateInfo)(implicit auth: Auth): Result[User] =
    if(auth.verifyPassword(user.password)) {
      val userWithPreparedValues = user.copy(newPassword = user.newPassword.map(Auth.hashPassword))
      processUserUpdate(auth.user, userWithPreparedValues)
    }else
      NotAuthorized
}
