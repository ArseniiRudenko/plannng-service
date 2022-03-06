package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.TimeApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralResult, Time}

object TimeApiMarshallerImpl extends TimeApiMarshaller with JacksonMarshaller {

  override implicit val fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time] = getUnmarshaller[Time]

  override implicit val toEntityMarshallerTime: ToEntityMarshaller[Time] = getMarshaller[Time]

  override implicit val toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]] = getMarshaller[Seq[Time]]

  override implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]
}
