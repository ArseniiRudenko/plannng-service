package work.arudenko.kanban.backend.controller

import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.{AdminUserApiService, UserApiService}
import work.arudenko.kanban.backend.model.{GeneralResult, NotAuthorized, NotFound, Result, SuccessEmpty, SuccessEntity, User, UserInfo, WrongInput}

object AdminUserApiServiceImpl extends AdminUserApiService with LazyLogging{

  override def getUser(id: Int): Result[User] =
    User.get(id) match {
      case Some(value) => SuccessEntity(value)
      case None => NotFound
    }


  override def getUser(knownInfo: UserInfo): Result[Seq[User]] = ???


  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def deleteUser(id: Int)(implicit auth: Auth):Result[Unit] =
    User.delete(id) match {
      case 0 => NotFound
      case 1 => SuccessEmpty
      case e =>
        logger.error(s"returned value $e for delete request of user id $id")
        GeneralResult(500,"db error")
    }


  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUsersWithArrayInput(user: Seq[UserInfo])(implicit auth: Auth):Result[User] =
      User.createUsers(user) match {
        case Some(value) => value match {
          case v if v.length == user.length => SuccessEmpty
          case v => WrongInput(s"created ${v.length} users out of ${user.length}")
        }
        case None => WrongInput(s"failed creating users")
      }

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def updateUser(user: User)(implicit auth: Auth):Result[User] = {
    User.get(user.id) match {
      case Some(oldUserValues) =>
        processUserUpdate(oldUserValues, ???) //TODO:update user
      case None => NotFound
    }
  }

}
