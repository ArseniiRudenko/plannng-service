package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.model.{Comment, GeneralResult, Result}
import work.arudenko.kanban.backend.AkkaHttpHelper._
import work.arudenko.kanban.backend.controller.Auth


class CommentApi(
    commentService: CommentApiService,
    commentMarshaller: CommentApiMarshaller
)extends AuthenticatedApi  {

  
  import commentMarshaller._

  override def route(implicit auth: Auth): Route =
    pathPrefix("comment") {
      concat(
        path("task" / IntNumber) { taskId =>
          concat(
            post {
              entity(as[String]) { comment =>
                commentService.addComment(taskId = taskId, comment = comment)
              }
            },
            get {
                commentService.getComments(taskId = taskId)
            }
          )
        },
        path(IntNumber) { commentId =>
            concat(
              delete {
                commentService.deleteComment(commentId = commentId)
              },
              get{
                commentService.getComment(commentId)
              }
            )
        },
        pathEndOrSingleSlash {
            put {
              entity(as[Comment]) { comment =>
                commentService.updateComment(comment = comment)
              }
            }
        }
      )
    }
}


trait CommentApiService {

  def getComment(commentId: Int)(implicit auth: Auth): Result[Comment]

  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def addComment(taskId: Int, comment: String)(implicit auth: Auth):Result[Comment]

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def deleteComment(commentId: Int)(implicit auth: Auth):Result[Comment]

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Comment]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def getComments(taskId: Int)(implicit auth: Auth):Result[Seq[Comment]]

  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def updateComment(comment:Comment)(implicit auth: Auth):Result[Comment]

}

trait CommentApiMarshaller {
  implicit val fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment]

  implicit val toEntityMarshallerComment: ToEntityMarshaller[Comment]

  implicit val toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]]

  implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]

}

