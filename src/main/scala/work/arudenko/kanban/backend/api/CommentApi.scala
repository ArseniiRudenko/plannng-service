package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.model.{Comment, GeneralError}
import work.arudenko.kanban.backend.AkkaHttpHelper._
import work.arudenko.kanban.backend.model.GeneralError


class CommentApi(
    commentService: CommentApiService,
    commentMarshaller: CommentApiMarshaller
) {

  
  import commentMarshaller._

  lazy val route: Route =
    path("task" / IntNumber / "comment") { (taskId) => 
      post {  
            entity(as[String]){ comment =>
              commentService.addComment(taskId = taskId, comment = comment)
            }
      }~
      get {
          commentService.getComments(taskId = taskId)
      }
    } ~
    path("comment" / IntNumber) { (commentId) => 
      delete {  
            commentService.deleteComment(commentId = commentId)
      }
    } ~
    path("comment") { 
      put {  
            entity(as[Comment]){ comment =>
              commentService.updateComment(comment = comment)
            }
      }
    }
}


trait CommentApiService {

  def Comment200(responseComment: Comment)(implicit toEntityMarshallerComment: ToEntityMarshaller[Comment]): Route =
    complete((200, responseComment))

  val Comment200: Route =
    complete((200, "Success"))
  val Comment404: Route =
    complete((404, "Task not found"))
  val Comment403: Route =
    complete((403, "Current user is not authorized to do that"))


  def Comment400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))

  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def addComment(taskId: Int, comment: String)
      (implicit toEntityMarshallerComment: ToEntityMarshaller[Comment], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def deleteComment(commentId: Int)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def getComments200(responseCommentarray: Seq[Comment])(implicit toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]]): Route =
    complete((200, responseCommentarray))

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Comment]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def getComments(taskId: Int)
      (implicit toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def updateComment(comment:Comment)
      (implicit toEntityMarshallerComment: ToEntityMarshaller[Comment], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

}

trait CommentApiMarshaller {
  implicit def fromEntityUnmarshallerComment: FromEntityUnmarshaller[Comment]

  implicit def toEntityMarshallerComment: ToEntityMarshaller[Comment]

  implicit def toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]]

  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]

}

