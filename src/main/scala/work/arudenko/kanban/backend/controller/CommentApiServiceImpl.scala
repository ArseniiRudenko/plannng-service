package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.CommentApiService
import work.arudenko.kanban.backend.model.{Comment, GeneralResult, NotAuthorized, NotFound, Result, SuccessEmpty, SuccessEntity, UserInfo}

import java.time.OffsetDateTime

object CommentApiServiceImpl extends CommentApiService  with LazyLogging{
  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def addComment(taskId: Int, comment: String)(implicit auth: Auth):Result[Comment] = {
        val id = Comment.create(auth.user.id,taskId,comment)
        SuccessEntity(Comment(id.toInt,comment,UserInfo(auth.user),OffsetDateTime.now()))
    }

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def deleteComment(commentId: Int)(implicit auth: Auth):Result[Comment]=
        if(auth.user.admin)
          Comment.delete(commentId) match {
            case 0 => NotFound
            case _ => SuccessEmpty
          }
        else NotAuthorized


  /**
   * Code: 200, Message: successful operation, DataType: Seq[Comment]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def getComments(taskId: Int)(implicit auth: Auth):Result[Seq[Comment]] =
     SuccessEntity(Comment.getByIssueId(taskId))


  /**
   * Code: 200, Message: successful operation, DataType: Comment
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def updateComment(comment: Comment)(implicit auth: Auth):Result[Comment] =
        if(comment.author.email.contains(auth.user.email.get)){ //check that we are changing comment for same user
            Comment.updateTextWithUserCheck(comment) match {
              case 1 => SuccessEntity(comment)
              case 0 => NotFound
              case u =>
                logger.error(s"unexpected update count $u when running update $comment for ${auth.user}")
                GeneralResult(500,"unexpected answer fro database")
            }
        }else{
            NotAuthorized
        }
}
