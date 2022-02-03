package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import work.arudenko.kanban.backend.api.UserApiService
import work.arudenko.kanban.backend.model.{GeneralError, User}

class UserApiServiceImpl extends UserApiService{
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUser(user: User)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUsersWithArrayInput(user: Seq[User])(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 200, Message: Success
   */
  override def createUsersWithListInput(user: Seq[User])(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def deleteUser(username: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    User.getId(username) match {
      case Some(value) => User.delete(value); deleteUser200
      case None => deleteUser404
    }

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def getUserByName(username: String)(implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    User.getUserData(username) match {
      case Some(value) => getUserByName200(value)
      case None => getUserByName404
    }
  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def loginUser(username: String, password: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def logoutUser()(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def updateUser(username: String, user: User)(implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???
}
