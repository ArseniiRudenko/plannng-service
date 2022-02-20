package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.TaskApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralResult, Task}

object TaskApiMarshallerImpl extends TaskApiMarshaller with JacksonMarshaller {

  override implicit def fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task] = getUnmarshaller[Task]

  override implicit def toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]] = getMarshaller[Seq[Task]]

  override implicit def toEntityMarshallerTask: ToEntityMarshaller[Task] = getMarshaller[Task]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]
}
