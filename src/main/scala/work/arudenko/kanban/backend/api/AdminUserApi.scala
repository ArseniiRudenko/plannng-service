package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server._
import Directives._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server
import akka.http.scaladsl.server.util.Tuple
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model.{Result, User, UserInfo}

import scala.concurrent.ExecutionContext

class AdminUserApi(
                    adminUserService: AdminUserApiService,
                    userMarshaller: AdminUserApiMarshaller
                  )(implicit ex:ExecutionContext)  extends AuthenticatedApi("admin") {

  import userMarshaller._

  private val authenticatorAdmin: server.Directives.Authenticator[Auth] =
    authenticator.andThen(_.flatMap(auth=>if(auth.user.admin) Some(auth) else None))

  override val authenticatedRoute:Route = pathPrefix("admin"){
    authenticateOAuth2("Global", authenticatorAdmin) {
      implicit auth => route
    }
  }


  override def route(implicit auth: Auth): Route =
    pathPrefix(  "user") {
      concat(
        pathEndOrSingleSlash {
          concat(
            put {
              entity(as[User]) { user =>
                adminUserService.updateUser(user = user)
              }
            },
            post {
              entity(as[UserInfo]) { user =>
                adminUserService.getUser(knownInfo = user)
              }
            }
          )
        },
        path(IntNumber) { id =>
          concat(
            delete {
              adminUserService.deleteUser(id)
            },
            get {
              adminUserService.getUser(id)
            }
          )
        },
        path( "createWithArray") {
          post {
            entity(as[Seq[UserInfo]]) { user =>
              adminUserService.createUsersWithArrayInput(user = user)
            }
          }
        }
      )
    }


}

trait AdminUserApiService{

  def getUser(id: Int): Result[User]
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def deleteUser(id: Int)(implicit auth: Auth): Result[Unit]

  def getUser(knownInfo: UserInfo): Result[Seq[User]]
  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def updateUser(user: User)(implicit auth: Auth): Result[User]

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUsersWithArrayInput(user: Seq[UserInfo])(implicit auth: Auth): Result[User]

}

trait AdminUserApiMarshaller{

  implicit val fromEntityUnmarshallerUser: FromEntityUnmarshaller[User]
  implicit val toEntityMarshallerUser: ToEntityMarshaller[User]
  implicit val toEntityMarshallerUserSeq: ToEntityMarshaller[Seq[User]]
  implicit val fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo]
  implicit val fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]]

}
