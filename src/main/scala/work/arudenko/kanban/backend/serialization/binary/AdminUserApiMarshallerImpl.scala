package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller

import work.arudenko.kanban.backend.api.AdminUserApiMarshaller
import work.arudenko.kanban.backend.model.{User, UserInfo}

object  AdminUserApiMarshallerImpl extends  AdminUserApiMarshaller with BoopickleMarshaller {
  import boopickle.Default._

  override implicit val toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]
  override implicit val fromEntityUnmarshallerUser: FromEntityUnmarshaller[User] = getUnmarshaller[User]

  override implicit val toEntityMarshallerUserSeq: ToEntityMarshaller[Seq[User]] = getMarshaller[Seq[User]]

  override implicit val fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]
  override implicit val fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo] = getUnmarshaller[UserInfo]
}
