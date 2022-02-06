package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.model.{GeneralError, User, UserCreationInfo, UserInfo, UserUpdateInfo}
import work.arudenko.kanban.backend.AkkaHttpHelper._

import java.time.OffsetDateTime


class UserApi(
    userService: UserApiService,
    userMarshaller: UserApiMarshaller
) {

  
  import userMarshaller._

  lazy val route: Route =
    path("user") { 
      post {
        entity(as[UserCreationInfo]){ user =>
          userService.createUser(user = user)
        }
      }~
      put {
        entity(as[UserUpdateInfo]){ user =>
          userService.updateUser(user = user)
        }
      }
    } ~
    path("user" / "createWithArray") { 
      post {  
            entity(as[Seq[UserCreationInfo]]){ user =>
              userService.createUsersWithArrayInput(user = user)
            }
      }
    } ~
    path("user" / Segment) { (username) => 
      delete {  
            userService.deleteUser(username = username)
      }
      get {
        userService.getUserByName(username = username)
      }
    } ~
    path("user" / "login") { 
      get { 
        parameters("username".as[String], "password".as[String]) { (username, password) => 
            userService.loginUser(username = username, password = password)
        }
      }
    } ~
    path("user" / "logout") { 
      get {  
            userService.logoutUser()
      }
    }
}


trait UserApiService {

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUser(user: UserCreationInfo)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  val User200: Route =
    complete((200, "Success"))
  val User404: Route =
    complete((404, "User not found"))
  val User403: Route =
    complete((403, "Current user is not authorized to do that"))

  def User400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))

  def getUserByName200(user:UserInfo)(implicit toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo]):Route =
    complete((200, user))

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUsersWithArrayInput(user: Seq[UserCreationInfo])
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def deleteUser(username: String)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

    /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def getUserByName(username: String)
      (implicit toEntityMarshallerUser: ToEntityMarshaller[UserInfo], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def loginUser200(responseString: String)(implicit toEntityMarshallerString: ToEntityMarshaller[String]): Route =
    complete((200, responseString))

  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def loginUser(username: String, password: String)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def logoutUser()
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def updateUser(user: UserUpdateInfo)
      (implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

}

trait UserApiMarshaller {
  implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo]
  implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo]
  implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserCreationInfo]]
  implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo]
  implicit def toEntityMarshallerUser: ToEntityMarshaller[User]
  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]

}

