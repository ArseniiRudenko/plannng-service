package work.arudenko.kanban.backend.model

import scalikejdbc._
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


  def deleteForUser(userId:Int,commentId:Int): Int =
    update(sql"delete from $table where id=$commentId and author=$userId")

  override def sqlExtractor(rs: WrappedResultSet): Comment =
    new Comment(
    rs.int("id"),
    rs.string("content"),
    UserInfo.sqlExtractor(rs),
    rs.offsetDateTime("created_at"))

  def getByIssueId(issueId:Int): immutable.Seq[Comment] =
    getList(sql"select * from $table c join ${User.table} u on c.author=u.id where c.issue=$issueId")

  override def get(id: Int): Option[Comment] =
    getOne(sql"select * from $table c join ${User.table} u on c.author=u.id where c.id=$id")


  def create(userId:Int,taskId:Int,text:String): Long =
    insert(sql"insert into $table (author,content,issue) values($userId,$text,$taskId)")


  def updateTextWithUserCheck(comment:Comment): Int ={
    update(
      sql"""update $table c
            set text=${comment.text}
            from ${User.table} u
            where
                c.id=${comment.id}
                and c.author=u.id
                and u.email=${comment.author.email}
         """)
  }

}
