package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.UserApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralError, User, UserCreationInfo, UserInfo, UserUpdateInfo}

object UserApiMarshallerImpl extends UserApiMarshaller{

  override implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo] = ???

  override implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo] = ???

  override implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = ???

  override implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo] = ???

  override implicit def toEntityMarshallerUser: ToEntityMarshaller[User] = ???

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = ???
}
