package work.arudenko.kanban.backend.model

import org.postgresql.util.PGInterval
import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.OffsetDateTime
import scala.collection.immutable

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
  tags: Option[Seq[Tag]],
  status: Option[String])

object Task extends WithCommonSqlOperations[Task] {

  override val tableName = "issues"

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
      None,
      Some(rs.string("cur_status"))
    )

  def getByStatus(status:String)(implicit session:DBSession): Seq[Task] =
    sql"select * from $tableName where cur_status=$status".map(rs=>sqlExtractor(rs)).list.apply()


  def getByHeader(header:String)(implicit session:DBSession): Seq[Task] =
    sql"select * from $tableName where header ~ $header".map(rs=>sqlExtractor(rs)).list.apply()

}
