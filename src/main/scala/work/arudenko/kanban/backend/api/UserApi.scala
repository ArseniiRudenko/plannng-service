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
    path("user" / "me") {
      authenticateOAuth2("Global", authenticator) {
        implicit auth =>
          put {
            entity(as[UserUpdateInfo]) { user =>
              userService.updateSelf(user = user)
            }
          } ~
          delete {
              userService.deleteUser(auth)
          } ~
          get {
              userService.getCurrentUser(auth)
          }~
          path("logout") {
            get {
              userService.logoutUser(auth)
            }
          }
      }
    } ~
    path("user" / "login") {
        post {
          entity(as[UserCreationInfo]){ userInfo =>
            userService.loginUser(username = userInfo.email, password = userInfo.password)
          }
        }~
        path("reset"/ Segment) { resetToken =>
          post {
            entity(as[String]) { newPassword =>
              userService.resetPassword(resetToken, newPassword)
            }
          } ~
          put{
            userService.requestPasswordReset(resetToken)
          }
        } ~
        path("activate"/ Segment) { emailToken =>
          post {
            userService.activateAccount(emailToken)
          }
        }
        path("register"){
          post {
            entity(as[UserCreationInfo]){ user =>
              userService.createUser(user = user)
            }
          }
        }
    }~
    path("user" / "info"){
      authenticateOAuth2("Global", authenticator) {
        implicit auth =>
          post {
            entity(as[String]) { user =>
              userService.getUserByName(username = user)
            }
          }
      }
    }~
    path("user" / "admin") {
      authenticateOAuth2("Global", adminAuthenticator) {
        implicit auth =>
          delete {
            entity(as[String]) { user =>
              userService.deleteUser(username = user)
            }
          }~
          put {
            entity(as[UserUpdateInfo]) { user =>
                userService.updateUser(user = user)
            }
          }
          path( "createWithArray") {
            post {
              entity(as[Seq[UserInfo]]) { user =>
                userService.createUsersWithArrayInput(user = user)
              }
            }
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
  def logoutUser(auth: Auth): Result[User]

  def requestPasswordReset(email: String): Result[User]

  def resetPassword(resetToken: String, newPassword: String): Result[User]

  def activateAccount(emailToken: String): Result[User]

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def updateUser(user: UserUpdateInfo)(implicit auth: Auth): Result[User]

  def updateSelf(user: UserUpdateInfo)(implicit auth: Auth): Result[User]

  def deleteUser(auth: Auth): Result[User]

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

  def getCurrentUser(auth: Auth): Result[UserInfo]
}

trait UserApiMarshaller{
  implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo]
  implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo]
  implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]]
  implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo]
  implicit def toEntityMarshallerUser: ToEntityMarshaller[User]
  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]

}

