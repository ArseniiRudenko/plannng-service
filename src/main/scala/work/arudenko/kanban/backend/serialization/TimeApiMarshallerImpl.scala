package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.TimeApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Time}
import boopickle.Default._

object TimeApiMarshallerImpl extends TimeApiMarshaller with BoopickleMarshaller {
  override implicit def fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time] = getUnmarshaller[Time]

  override implicit def toEntityMarshallerTime: ToEntityMarshaller[Time] = getMarshaller[Time]

  override implicit def toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]] = getMarshaller[Seq[Time]]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = getMarshaller[GeneralError]
}
