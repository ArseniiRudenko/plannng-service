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
            entity(as[Comment]){ comment =>
              commentService.addComment(taskId = taskId, comment = comment)
            }
      }
    } ~
    path("comment" / IntNumber) { (commentId) => 
      delete {  
            commentService.deleteComment(commentId = commentId)
      }
    } ~
    path("task" / IntNumber / "comment") { (taskId) => 
      get {  
            commentService.getComments(taskId = taskId)
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

  def addComment200(responseComment: Comment)(implicit toEntityMarshallerComment: ToEntityMarshaller[Comment]): Route =
    complete((200, responseComment))
  def addComment400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  def Comment404: Route =
    complete((404, "Task not found"))
  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def addComment(taskId: Int, comment: Comment)
      (implicit toEntityMarshallerComment: ToEntityMarshaller[Comment], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def deleteComment200: Route =
    complete((200, "successful operation"))
  def deleteComment400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))

  def deleteComment403: Route =
    complete((403, "Only admins can remove tasks"))
  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def deleteComment(commentId: Int)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def getComments200(responseCommentarray: Seq[Comment])(implicit toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]]): Route =
    complete((200, responseCommentarray))
  def getComments400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Comment]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def getComments(taskId: Int)
      (implicit toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def updateComment200(responseComment: Comment)(implicit toEntityMarshallerComment: ToEntityMarshaller[Comment]): Route =
    complete((200, responseComment))
  def updateComment400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
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

