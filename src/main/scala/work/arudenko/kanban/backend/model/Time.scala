package work.arudenko.kanban.backend.model

import org.postgresql.util.PGInterval
import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * @param id  for example: ''null''
 * @param description  for example: ''null''
 * @param date  for example: ''null''
 * @param time  for example: ''null''
 * @param createdAt  for example: ''null''
*/
final case class Time (
  id: Option[Int],
  description: String,
  date: LocalDate,
  time: Int,
  createdAt: Option[OffsetDateTime]
)

object Time extends WithCommonSqlOperations[Time]{
  override def sqlExtractor(rs: WrappedResultSet): Time =
    new Time(
      Some(rs.int("id")),
      rs.string("comment"),
      rs.localDate("on_date"),
      rs.get[PGInterval]("time").getHours,
      Some(rs.offsetDateTime("created_at"))
    )

  override val tableName = "project_track.spent_time"

  def getByUser(userId:Int) = getList(sql"select * from $tbl where person=$userId")

  def getByTask(taskId:Int) = getList(sql"select * from $tbl where issue=$taskId")

}

