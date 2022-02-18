package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.model._

object UserApiMarshallerImpl extends UserApiMarshaller with BoopickleMarshaller {

  import boopickle.Default._

  override implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo] = getUnmarshaller[UserCreationInfo]

  override implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo] = getUnmarshaller[UserUpdateInfo]

  override implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]

  override implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo] = getMarshaller[UserInfo]

  override implicit def toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = getMarshaller[GeneralError]
}
