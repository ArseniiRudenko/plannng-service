package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.UserApiMarshaller
import work.arudenko.kanban.backend.model._

object UserApiMarshallerImpl extends UserApiMarshaller with JacksonMarshaller {

  override implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo] = getUnmarshaller[UserCreationInfo]

  override implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo] = getUnmarshaller[UserUpdateInfo]

  override implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]

  override implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo] = getMarshaller[UserInfo]

  override implicit def toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]

  override implicit def fromEntityUnmarshallerUser: FromEntityUnmarshaller[User] = getUnmarshaller[User]

  override implicit def toEntityMarshallerUserSeq: ToEntityMarshaller[Seq[User]] = getMarshaller[Seq[User]]
}
