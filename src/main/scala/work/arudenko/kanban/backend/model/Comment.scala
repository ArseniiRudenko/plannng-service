package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.model.Task.{sqlExtractor, tbl}
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.OffsetDateTime
import scala.collection.immutable

/**
 * @param id  for example: ''null''
 * @param text  for example: ''null''
 * @param author  for example: ''null''
 * @param createdAt  for example: ''null''
*/
final case class Comment (
  id: Int,
  text: String,
  author: UserInfo,
  createdAt: OffsetDateTime
)


object Comment extends WithCommonSqlOperations[Comment] {

  override val tableName = "project_track.issue_comments"

  protected override val curSyntax = syntax("c")

  def deleteForUser(userId:Int,commentId:Int): Int =
    update(sql"delete from $tbl where id=$commentId and author=$userId")

  override def sqlExtractor(rs: WrappedResultSet): Comment =
    new Comment(
    rs.int("id"),
    rs.string("content"),
    UserInfo(User.sqlExtractor(rs)),
    rs.offsetDateTime("created_at"))

  def getByIssueId(issueId:Int): immutable.Seq[Comment] =
    getList(sql"select * from $tbl join ${User.tbl} on c.author=m.id where issue=$issueId")

  override def get(id: Int): Option[Comment] =
    getOne(sql"select * from $tbl join ${User.tbl} on c.author=m.id where c.id=$id")


  def create(userId:Int,taskId:Int,text:String): Long =
    insert(sql"insert into $tbl (author,content,issue) values($userId,$text,$taskId)")


  def updateTextWithUserCheck(comment:Comment): Int ={
    update(sql"update $tbl set text=${comment.text} from ${User.tbl} where c.id=${comment.id} and c.author=u.id and u.email=${comment.author.email}")
  }

}
