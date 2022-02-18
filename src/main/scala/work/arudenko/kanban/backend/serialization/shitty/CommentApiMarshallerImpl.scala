package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.model.{Comment, GeneralError}

object CommentApiMarshallerImpl extends CommentApiMarshaller with JacksonMarshaller {

  override implicit def fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment] =
    getUnmarshaller[Comment]

  override implicit def toEntityMarshallerComment: ToEntityMarshaller[Comment] =
    getMarshaller[Comment]

  override implicit def toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]] =
    getMarshaller[Seq[Comment]]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] =
    getMarshaller[GeneralError]
}
