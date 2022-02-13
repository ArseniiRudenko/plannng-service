package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromStringUnmarshaller}
import work.arudenko.kanban.backend.api.TaskApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Task}
import boopickle.Default._
import java.io.File

object TaskApiMarshallerImpl extends TaskApiMarshaller with BoopickleMarshaller {
  override implicit def fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task] = getUnmarshaller[Task]

  override implicit def toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]] = getMarshaller[Seq[Task]]

  override implicit def toEntityMarshallerTask: ToEntityMarshaller[Task] = getMarshaller[Task]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = getMarshaller[GeneralError]
}
