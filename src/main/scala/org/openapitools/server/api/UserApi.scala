package org.openapitools.server.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import org.openapitools.server.AkkaHttpHelper._
import org.openapitools.server.model.GeneralError
import java.time.OffsetDateTime
import org.openapitools.server.model.User


class UserApi(
    userService: UserApiService,
    userMarshaller: UserApiMarshaller
) {

  
  import userMarshaller._

  lazy val route: Route =
    path("user") { 
      post {  
            entity(as[User]){ user =>
              userService.createUser(user = user)
            }
      }
    } ~
    path("user" / "createWithArray") { 
      post {  
            entity(as[Seq[User]]){ user =>
              userService.createUsersWithArrayInput(user = user)
            }
      }
    } ~
    path("user" / "createWithList") { 
      post {  
            entity(as[Seq[User]]){ user =>
              userService.createUsersWithListInput(user = user)
            }
      }
    } ~
    path("user" / Segment) { (username) => 
      delete {  
            userService.deleteUser(username = username)
      }
    } ~
    path("user" / Segment) { (username) => 
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
    } ~
    path("user" / Segment) { (username) => 
      put {  
            entity(as[User]){ user =>
              userService.updateUser(username = username, user = user)
            }
      }
    }
}


trait UserApiService {

  def createUser200: Route =
    complete((200, "Success"))
  def createUser400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUser(user: User)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def createUsersWithArrayInput200: Route =
    complete((200, "Success"))
  def createUsersWithArrayInput400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def createUsersWithArrayInput(user: Seq[User])
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def createUsersWithListInput400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  def createUsersWithListInput200: Route =
    complete((200, "Success"))
  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 200, Message: Success
   */
  def createUsersWithListInput(user: Seq[User])
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def deleteUser200: Route =
    complete((200, "Success"))
  def deleteUser400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  def deleteUser404: Route =
    complete((404, "User not found"))
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def deleteUser(username: String)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def getUserByName200(responseUser: User)(implicit toEntityMarshallerUser: ToEntityMarshaller[User]): Route =
    complete((200, responseUser))
  def getUserByName400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  def getUserByName404: Route =
    complete((404, "User not found"))
  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def getUserByName(username: String)
      (implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def loginUser200(responseString: String)(implicit toEntityMarshallerString: ToEntityMarshaller[String]): Route =
    complete((200, responseString))
  def loginUser400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def loginUser(username: String, password: String)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def logoutUser200: Route =
    complete((200, "Success"))
  def logoutUser400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def logoutUser()
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def updateUser200(responseUser: User)(implicit toEntityMarshallerUser: ToEntityMarshaller[User]): Route =
    complete((200, responseUser))
  def updateUser400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  def updateUser404: Route =
    complete((404, "User not found"))
  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  def updateUser(username: String, user: User)
      (implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

}

trait UserApiMarshaller {
  implicit def fromEntityUnmarshallerUser: FromEntityUnmarshaller[User]

  implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[User]]



  implicit def toEntityMarshallerUser: ToEntityMarshaller[User]

  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]

}

