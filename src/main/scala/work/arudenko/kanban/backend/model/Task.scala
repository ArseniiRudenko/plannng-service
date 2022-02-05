package work.arudenko.kanban.backend.model

import org.postgresql.util.PGInterval
import scalikejdbc._
import work.arudenko.kanban.backend.model.Comment.{tbl, update}
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.OffsetDateTime

/**
 * @param id  for example: ''null''
 * @param header  for example: ''do this''
 * @param description  for example: ''this is what should be the result''
 * @param parentId  for example: ''null''
 * @param deadline  for example: ''null''
 * @param assigneeId  for example: ''null''
 * @param estimatedTime  for example: ''null''
 * @param photoUrls  for example: ''null''
 * @param tags  for example: ''null''
 * @param status task status for example: ''null''
*/
final case class Task (
  id: Option[Int],
  header: String,
  description: Option[String],
  priority:String,
  parentId: Option[Int],
  deadline: Option[OffsetDateTime],
  assigneeId: Option[Int],
  estimatedTime: Option[Int],
  tags: Seq[Tag],
  status: Option[String])

object Task extends WithCommonSqlOperations[Task] {

  override val tableName = "project_track.issues"

  override def sqlExtractor(rs: WrappedResultSet):Task =
    new Task(
      Some(rs.int("id")),
      rs.string("header"),
      rs.stringOpt("description"),
      rs.string("priority"),
      rs.intOpt("parent"),
      rs.offsetDateTimeOpt("deadline"),
      rs.intOpt("assignee"),
      rs.getOpt[PGInterval]("estimated_time").map(_.getHours),
      Tag.getTagsForIssue(rs.int("id")),
      Some(rs.string("cur_status"))
    )

  def getByStatus(status:String): Seq[Task] =
    getList(sql"select * from $tbl where cur_status=$status::project_track.status")

  def getByTagId(id:Int): Seq[Task] =
    getList(sql"select m.* from $tbl  join  project_track.tag_to_issue b on m.id=b.issue_id where b.tag_id=$id")

  def getByTagIds(id:Seq[Int]): Seq[Task] =
    getList(sql"select m.* from $tbl  join  project_track.tag_to_issue b on m.id=b.issue_id where b.tag_id in ${id}")

  def getByHeader(header:String): Seq[Task] =DB readOnly { implicit session =>
    getList(sql"select * from $tbl where header ~ $header")
  }



}
