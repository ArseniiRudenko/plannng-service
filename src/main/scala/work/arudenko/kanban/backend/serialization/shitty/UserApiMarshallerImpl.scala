package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.UserApiMarshaller
import work.arudenko.kanban.backend.model._

object UserApiMarshallerImpl extends UserApiMarshaller with JacksonMarshaller {

  override implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[SignUpInfo] = getUnmarshaller[SignUpInfo]

  override implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo] = getUnmarshaller[UserUpdateInfo]

  override implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo] = getMarshaller[UserInfo]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]


}
