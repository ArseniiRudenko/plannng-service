package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.TaskApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralResult, Task}

object TaskApiMarshallerImpl extends TaskApiMarshaller with BoopickleMarshaller {

  import boopickle.Default._

  override implicit val fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task] = getUnmarshaller[Task]

  override implicit val toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]] = getMarshaller[Seq[Task]]

  override implicit val toEntityMarshallerTask: ToEntityMarshaller[Task] = getMarshaller[Task]

  override implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] = getMarshaller[GeneralResult]
}
