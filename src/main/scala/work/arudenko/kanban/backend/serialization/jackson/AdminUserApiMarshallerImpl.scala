package work.arudenko.kanban.backend.serialization.jackson

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.{AdminUserApiMarshaller, UserApiMarshaller}
import work.arudenko.kanban.backend.model.{GeneralResult, SignUpInfo, User, UserInfo, UserUpdateInfo}
import work.arudenko.kanban.backend.serialization.jackson.UserApiMarshallerImpl.{getMarshaller, getUnmarshaller}

object  AdminUserApiMarshallerImpl extends  AdminUserApiMarshaller with JacksonMarshaller {

  override implicit val toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]
  override implicit val fromEntityUnmarshallerUser: FromEntityUnmarshaller[User] = getUnmarshaller[User]

  override implicit val toEntityMarshallerUserSeq: ToEntityMarshaller[Seq[User]] = getMarshaller[Seq[User]]

  override implicit val fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]
  override implicit val fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo] = getUnmarshaller[UserInfo]
}
