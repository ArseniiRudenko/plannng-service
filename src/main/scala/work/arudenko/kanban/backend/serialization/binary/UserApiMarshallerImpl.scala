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

  val userParser: Parse[User] = Parse(arr=>Unpickle[User].fromBytes(ByteBuffer.wrap(arr)))

  val userSerializer:Format = Format(
    {
      case u:User =>{
        val buf = Pickle.intoBytes(u)
        val arr = Array.ofDim[Byte](buf.remaining)
        buf.get(arr)
        arr
      }
    }
  )

  override implicit def fromEntityUnmarshallerUserCreate: FromEntityUnmarshaller[UserCreationInfo] = getUnmarshaller[UserCreationInfo]

  override implicit def fromEntityUnmarshallerUserUpdate: FromEntityUnmarshaller[UserUpdateInfo] = getUnmarshaller[UserUpdateInfo]

  override implicit def fromEntityUnmarshallerUserList: FromEntityUnmarshaller[Seq[UserInfo]] = getUnmarshaller[Seq[UserInfo]]

  override implicit def toEntityMarshallerUserInfo: ToEntityMarshaller[UserInfo] = getMarshaller[UserInfo]

  override implicit def toEntityMarshallerUser: ToEntityMarshaller[User] = getMarshaller[User]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]
}
