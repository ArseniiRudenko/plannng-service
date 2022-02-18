package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Task}

object TaskApiMarshallerImpl extends TaskApiMarshaller with BoopickleMarshaller {

  import boopickle.Default._

  override implicit def fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task] = getUnmarshaller[Task]

  override implicit def toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]] = getMarshaller[Seq[Task]]

  override implicit def toEntityMarshallerTask: ToEntityMarshaller[Task] = getMarshaller[Task]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = getMarshaller[GeneralError]
}
