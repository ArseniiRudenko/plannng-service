package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller

import work.arudenko.kanban.backend.api.AdminUserApiMarshaller
import work.arudenko.kanban.backend.model.{User, UserInfo}

object  AdminUserApiMarshallerImpl extends  AdminUserApiMarshaller with BoopickleMarshaller {
  import boopickle.Default._

  override implicit def toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]
  override implicit def fromEntityUnmarshallerUser: FromEntityUnmarshaller[User] = getUnmarshaller[User]

  override implicit def toEntityMarshallerUserSeq: ToEntityMarshaller[Seq[User]] = getMarshaller[Seq[User]]

  override implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]
  override implicit def fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo] = getUnmarshaller[UserInfo]
}
