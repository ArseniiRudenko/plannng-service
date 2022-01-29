package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.OffsetDateTime

/**
 * @param id  for example: ''null''
 * @param text  for example: ''null''
 * @param author  for example: ''null''
 * @param createdAt  for example: ''null''
*/
final case class Comment (
  id: Option[Int],
  text: String,
  author: Option[Int],
  createdAt: Option[OffsetDateTime]
)


object Comment extends WithCommonSqlOperations[Comment] {

  override val tableName = "issue_comments"


  override def sqlExtractor(rs: WrappedResultSet): Comment =
    new Comment(
    Some(rs.int("id")),
    rs.string("content"),
    rs.intOpt("author"),
    Some(rs.offsetDateTime("created_at")))

  

}
