package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.CommentApiService
import work.arudenko.kanban.backend.model.{Comment, GeneralError}

class CommentApiServiceImpl extends CommentApiService  with LazyLogging with AuthenticatedRoute{
  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def addComment(taskId: Int, comment: Comment)(implicit toEntityMarshallerComment: ToEntityMarshaller[Comment], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def deleteComment(commentId: Int)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      auth =>
        if(auth.user.admin)
          Comment.delete(commentId) match {
            case 0 => Comment404
            case _ => deleteComment200
          }
        else deleteComment403
    }

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Comment]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def getComments(taskId: Int)(implicit toEntityMarshallerCommentarray: ToEntityMarshaller[Seq[Comment]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    getComments200(Comment.getByIssueId(taskId))

  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def updateComment(comment: Comment)(implicit toEntityMarshallerComment: ToEntityMarshaller[Comment], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???
}
