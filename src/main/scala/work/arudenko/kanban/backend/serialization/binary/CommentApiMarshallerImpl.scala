package work.arudenko.kanban.backend.serialization.binary

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.CommentApiMarshaller
import work.arudenko.kanban.backend.model.{Comment, GeneralResult}

object CommentApiMarshallerImpl extends CommentApiMarshaller with BoopickleMarshaller {
  import boopickle.Default._

  override implicit def fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment] =
    getUnmarshaller[Comment]

  override implicit def toEntityMarshallerComment: ToEntityMarshaller[Comment] =
    getMarshaller[Comment]

  override implicit def toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]] =
    getMarshaller[Seq[Comment]]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult] =
    getMarshaller[GeneralResult]
}
