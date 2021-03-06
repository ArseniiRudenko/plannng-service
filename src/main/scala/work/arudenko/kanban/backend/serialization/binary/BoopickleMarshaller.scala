package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.byteStringUnmarshaller
import akka.util.ByteString
import boopickle.{DecoderSpeed, Default, EncoderSpeed}

import java.nio.ByteBuffer
import java.time._

trait BoopickleMarshaller {

  import boopickle.Default._


  implicit val instantPickler: Pickler[Instant] = transformPickler[Instant, Long](vl => Instant.ofEpochSecond(vl))(vl => vl.getEpochSecond)
  implicit val offsetDateTimePickler: Pickler[OffsetDateTime] = transformPickler[OffsetDateTime, (Instant, String)](dt => OffsetDateTime.ofInstant(dt._1, ZoneId.of(dt._2)))(offset => (offset.toInstant, ZoneId.from(offset).getId))
  implicit val localDatePickler: Pickler[LocalDate] = transformPickler[LocalDate, Long](ed => LocalDate.ofEpochDay(ed))(ld => ld.toEpochDay)
  implicit val loclalTimePickler: Pickler[LocalTime] = transformPickler[LocalTime, Int](lg => LocalTime.ofSecondOfDay(lg))(lt => lt.toSecondOfDay)

  private implicit def toByteString(bb: ByteBuffer): ByteString = ByteString(bb)

  protected def getMarshaller[T](implicit pickler: Pickler[T]): Marshaller[T, RequestEntity] =
    Marshaller.combined[T, ByteString, RequestEntity](c => Pickle.intoBytes(c))

  protected def getUnmarshaller[T](implicit p: Pickler[T]) =
    byteStringUnmarshaller.map(bytes => Unpickle[T].fromBytes(bytes.asByteBuffer))

}
