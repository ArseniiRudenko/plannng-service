package work.arudenko.kanban.backend.api

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model._

import scala.concurrent.ExecutionContext


class UserApi(
    userService: UserApiService,
    userMarshaller: UserApiMarshaller
) (implicit ex:ExecutionContext)extends AuthenticatedApi("user") {

  import userMarshaller._

  lazy val loginRoute:Route =
    pathPrefix( "login") {
      concat(
        pathEndOrSingleSlash {
          post {
            entity(as[SignUpInfo]) { userInfo =>
              userService.loginUser(username = userInfo.email, password = userInfo.password)
            }
          }
        },
        path("reset" / Segment) { resetToken =>
          concat(
            post {
              entity(as[String]) { newPassword =>
                userService.resetPassword(resetToken, newPassword)
              }
            },
            put {
              userService.requestPasswordReset(resetToken)
            }
          )
        },
        path("activate" / Segment) { emailToken =>
          post {
            userService.activateAccount(emailToken)
          }
        },
        path("register") {
          post {
            entity(as[SignUpInfo]) { user =>
              userService.createUser(user = user)
            }
          }
        }
      )
    }


  override def route(implicit auth: Auth): Route =

      concat(
        pathEndOrSingleSlash {
            concat(
              put {
                entity(as[UserUpdateInfo]) { user =>
                  userService.updateSelf(user = user)
                }
              },
              delete {
                  userService.deleteUser(auth)
              },
              get {
                  userService.getCurrentUser(auth)
              }
            )
        },
        path("logout") {
              get {
                userService.logoutUser(auth)
              }
        },
        path("info") {
            post {
              entity(as[String]) { user =>
                userService.getUserByEmail(username = user)
              }
            }
        },
        path(IntNumber){ id=>
          get {
            userService.getUser(id)
          }
        }
      )

}


trait UserApiService {

  def getUser(id: Int)(implicit auth: Auth): Result[UserInfo]
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUser(user: SignUpInfo): Result[Unit]

  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def loginUser(username: String, password: String): Result[String]

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def logoutUser(auth: Auth): Result[Unit]

  def requestPasswordReset(email: String): Result[Unit]

  def resetPassword(resetToken: String, newPassword: String): Result[Unit]

  def activateAccount(emailToken: String): Result[Unit]

  def updateSelf(user: UserUpdateInfo)(implicit auth: Auth): Result[Unit]

  def deleteUser(auth: Auth): Result[Unit]



  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def getUserByEmail(username: String)(implicit auth: Auth): Result[UserInfo]

  def getCurrentUser(auth: Auth): Result[UserInfo]


}

trait UserApiMarshaller{
  implicit val fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[SignUpInfo]
  implicit val fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo]
  implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]
  implicit val toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo]

}

