package work.arudenko.kanban.backend.serialization

import akka.http.scaladsl.unmarshalling._
import PredefinedFromEntityUnmarshallers._
import akka.http.scaladsl.marshalling._
import PredefinedToEntityMarshallers._
import akka.http.scaladsl.model.{HttpEntity, RequestEntity}
import akka.util.ByteString
import boopickle.{DecoderSpeed, Default, EncoderSpeed}
import work.arudenko.kanban.backend.api._
import work.arudenko.kanban.backend.model.{Comment, GeneralError}
import boopickle.Default._
import java.nio.ByteBuffer

object CommentApiMarshallerImpl extends CommentApiMarshaller with BoopickleMarshaller {


  override implicit def fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment] =
    getUnmarshaller[Comment]

  override implicit def toEntityMarshallerComment: ToEntityMarshaller[Comment] =
    getMarshaller[Comment]

  override implicit def toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]] =
    getMarshaller[Seq[Comment]]

  override implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError] =
    getMarshaller[GeneralError]
}
