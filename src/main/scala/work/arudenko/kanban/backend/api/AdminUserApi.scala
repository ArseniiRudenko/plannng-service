package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server._
import Directives._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model.{Result, User, UserInfo}

class AdminUserApi(
                    adminUserService: AdminUserApiService,
                    userMarshaller: AdminUserApiMarshaller
                  )  extends AuthenticatedApi {

  import userMarshaller._


  override def route(implicit auth: Auth): Route =
    pathPrefix( "admin" / "user") {
          put {
            entity(as[User]) { user =>
              adminUserService.updateUser(user = user)
            }
          } ~
          path(IntNumber){ id=>
            delete {
              adminUserService.deleteUser(id)
            } ~
            get{
              adminUserService.getUser(id)
            }
          }~
          post {
            entity(as[UserInfo]) { user =>
              adminUserService.getUser(knownInfo = user)
            }
          }~
          path( "createWithArray") {
            post {
              entity(as[Seq[UserInfo]]) { user =>
                adminUserService.createUsersWithArrayInput(user = user)
              }
            }
          }
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

  implicit def fromEntityUnmarshallerUser: FromEntityUnmarshaller[User]
  implicit def toEntityMarshallerUser: ToEntityMarshaller[User]
  implicit def toEntityMarshallerUserSeq: ToEntityMarshaller[Seq[User]]
  implicit def fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo]
  implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]]

}
