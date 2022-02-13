package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromStringUnmarshaller}
import work.arudenko.kanban.backend.api.TaskApiMarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Task}

import java.io.File

object TaskApiMarshallerImpl extends TaskApiMarshaller{
  override implicit def fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task] = ???

  override implicit def fromStringUnmarshallerFileList: FromStringUnmarshaller[Seq[File]] = ???

  override implicit def toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]] = ???

  override implicit def toEntityMarshallerTask: ToEntityMarshaller[Task] = ???

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = ???
}
