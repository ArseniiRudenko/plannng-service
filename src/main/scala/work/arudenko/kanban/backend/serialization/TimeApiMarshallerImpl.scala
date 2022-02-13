package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.TimeApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Time}

object TimeApiMarshallerImpl extends TimeApiMarshaller{
  override implicit def fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time] = ???

  override implicit def toEntityMarshallerTime: ToEntityMarshaller[Time] = ???

  override implicit def toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]] = ???

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = ???
}
