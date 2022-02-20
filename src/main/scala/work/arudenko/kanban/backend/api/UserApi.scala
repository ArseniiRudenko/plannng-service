package work.arudenko.kanban.backend.api

import akka.actor.Status.Success
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.model.{GeneralResult, Result, SuccessEmpty, SuccessEntity, User, UserCreationInfo, UserInfo, UserUpdateInfo}
import work.arudenko.kanban.backend.AkkaHttpHelper._
import work.arudenko.kanban.backend.controller.Auth

import java.time.OffsetDateTime
import scala.util.{Failure, Try}


class UserApi(
    userService: UserApiService,
    userMarshaller: UserApiMarshaller
) extends GenericApi {


  import userMarshaller._

  lazy val route: Route =
    path("user") { 
      post {
        entity(as[UserCreationInfo]){ user =>
          userService.createUser(user = user)
        }
      }~
      put {
        authenticateOAuth2("Global", authenticator) {
          implicit auth =>
          entity(as[UserUpdateInfo]) { user =>
            userService.updateUser(user = user)
          }
        }
      }
    } ~
      path("user" / "login") {
        post {
          entity(as[UserUpdateInfo]){ userInfo =>
            userService.loginUser(username = userInfo.email, password = userInfo.password)
          }
        }
      } ~
      path("user" / "login"/ "reset"/ Segment) { resetToken =>
        post {
          entity(as[String]) { newPassword =>
            userService.resetPassword(resetToken, newPassword)
          }
        } ~
        put{
            userService.requestPasswordReset(resetToken)
        }
      } ~
      path("user" / "login"/ "activate"/ Segment) { emailToken =>
        post {
          userService.activateAccount(emailToken)
        }
      }~
    path("user" / "createWithArray") { 
      post {
        authenticateOAuth2("Global", authenticator) {
          implicit auth =>  
            entity(as[Seq[UserInfo]]) { user =>
              userService.createUsersWithArrayInput(user = user)
            }
        }
      }
    } ~
    path("user" / Segment) { (username) =>
      authenticateOAuth2("Global", authenticator) {
        implicit auth =>
          delete {
            userService.deleteUser(username = username)
          }
          get {
            userService.getUserByName(username = username)
          }
      }
    } ~
    path("user" / "logout") { 
      get {
        authenticateOAuth2("Global", authenticator) {
          implicit auth =>
            userService.logoutUser(auth)
        }
      }
    }

}


trait UserApiService {

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUser(user: UserCreationInfo): Result[UserInfo]

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUsersWithArrayInput(user: Seq[UserInfo])(implicit auth: Auth): Result[User]


  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def loginUser(username: String, password: String): Result[String]

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def logoutUser(implicit auth: Auth): Result[User]

  def requestPasswordReset(email: String): Result[User]

  def resetPassword(resetToken: String, newPassword: String): Result[User]

  def activateAccount(emailToken: String): Result[User]

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def updateUser(user: UserUpdateInfo)(implicit auth: Auth): Result[User]


  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def deleteUser(username: String)(implicit auth: Auth): Result[User]

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def getUserByName(username: String)(implicit auth: Auth): Result[UserInfo]
}

trait UserApiMarshaller{
  implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo]
  implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo]
  implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]]
  implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo]
  implicit def toEntityMarshallerUser: ToEntityMarshaller[User]
  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]

}

