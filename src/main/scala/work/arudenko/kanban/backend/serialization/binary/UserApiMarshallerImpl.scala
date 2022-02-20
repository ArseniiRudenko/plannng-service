package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import boopickle.{DecoderSpeed, Default, EncoderSpeed}
import com.redis.serialization.{Format, Parse}
import work.arudenko.kanban.backend.api.UserApiMarshaller
import work.arudenko.kanban.backend.model._

import java.nio.ByteBuffer

object UserApiMarshallerImpl extends UserApiMarshaller with BoopickleMarshaller {

  import boopickle.Default._

  val userParser: Parse[User] = Parse(arr=>Unpickle[User].fromBytes(ByteBuffer.wrap(arr))(unpickleState))
  val userSerializer:Format = Format({case u:User => Pickle.intoBytes(u)(pickleState,implicitly[Pickler[User]]).array()})

  override implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo] = getUnmarshaller[UserCreationInfo]

  override implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo] = getUnmarshaller[UserUpdateInfo]

  override implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]

  override implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo] = getMarshaller[UserInfo]

  override implicit def toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]
}
