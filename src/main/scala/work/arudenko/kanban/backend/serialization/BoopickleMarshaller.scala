package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.byteStringUnmarshaller
import akka.util.ByteString
import boopickle.{DecoderSpeed, Default, EncoderSpeed}
import work.arudenko.kanban.backend.model.Comment
import work.arudenko.kanban.backend.serialization.CommentApiMarshallerImpl.unpickleState

import java.nio.ByteBuffer

trait BoopickleMarshaller {

  import boopickle.Default._
  protected val pickleState: PickleState = new PickleState(new EncoderSpeed)
  protected val unpickleState: ByteBuffer => Default.UnpickleState = (b: ByteBuffer) => new UnpickleState(new DecoderSpeed(b))

  private implicit def toByteString(bb: ByteBuffer):ByteString = ByteString(bb)

  protected def getMarshaller[T](implicit pickler: Pickler[T]): Marshaller[T, RequestEntity] =
    Marshaller.combined[T,ByteString,RequestEntity](c=>Pickle.intoBytes(c)(pickleState,implicitly[Pickler[T]]))

  protected def getUnmarshaller[T](implicit p:Pickler[T])=
    byteStringUnmarshaller.map(bytes=> Unpickle[T].fromBytes(bytes.asByteBuffer)(unpickleState))

}
