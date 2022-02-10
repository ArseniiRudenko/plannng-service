package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import scala.collection.immutable
import work.arudenko.kanban.backend.orm.SqlContext.TryLogged
import scala.util.Try
/**
 * @param id  for example: ''null''
 * @param name  for example: ''null''
 * @param description  for example: ''null''
*/
final case class Tag (
  id: Option[Int],
  name: String,
  description: Option[String]
)

object Tag extends WithCommonSqlOperations[Tag]{

  override val tableName = "project_track.tags"

  override def sqlExtractor(rs: WrappedResultSet): Tag =
    new Tag(
      Some(rs.int("id")),
      rs.string("name"),
      rs.stringOpt("description")
    )

  def getTagsForIssue(issueId:Int): immutable.Seq[Tag] =
    getList(
      sql"select * from $table t join project_track.tag_to_issue b on t.id=b.tag_id where b.issue_id=$issueId"
    )

  def getTagsByName(strMatch:String): immutable.Seq[Tag] =
    getList(sql"select * from $table where name ~ $strMatch")

  def createTag(tag: Tag):Option[Long] =
    Try(insert(sql"insert into $table (name,description) values (${tag.name},${tag.description})")).toOptionLogged

}

