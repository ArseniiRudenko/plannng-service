package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Time}

object TimeApiMarshallerImpl extends TimeApiMarshaller with BoopickleMarshaller {

  import boopickle.Default._

  override implicit def fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time] = getUnmarshaller[Time]

  override implicit def toEntityMarshallerTime: ToEntityMarshaller[Time] = getMarshaller[Time]

  override implicit def toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]] = getMarshaller[Seq[Time]]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = getMarshaller[GeneralError]
}
