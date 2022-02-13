package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api._
import work.arudenko.kanban.backend.model.{Comment, GeneralError}

object CommentApiMarshallerImpl extends CommentApiMarshaller{

  override implicit def fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment] = ???

  override implicit def toEntityMarshallerComment: ToEntityMarshaller[Comment] = ???

  override implicit def toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]] = ???

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] = ???
}
