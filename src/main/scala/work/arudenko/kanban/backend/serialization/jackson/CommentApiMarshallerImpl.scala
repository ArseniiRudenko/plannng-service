package work.arudenko.kanban.backend.serialization.jackson

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.CommentApiMarshaller
import work.arudenko.kanban.backend.model.{Comment, GeneralResult}

object CommentApiMarshallerImpl extends CommentApiMarshaller with JacksonMarshaller {

  override implicit val fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment] =
    getUnmarshaller[Comment]

  override implicit val toEntityMarshallerComment: ToEntityMarshaller[Comment] =
    getMarshaller[Comment]

  override implicit val toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]] =
    getMarshaller[Seq[Comment]]

  override implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] =
    getMarshaller[GeneralResult]
}
