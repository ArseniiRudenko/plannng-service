package work.arudenko.kanban.backend.model

import org.postgresql.util.PGInterval
import scalikejdbc._
import work.arudenko.kanban.backend.orm.SqlContext.TryLogged
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.LocalDate
import java.time.OffsetDateTime
import scala.collection.immutable
import scala.util.Try

/**
 * @param id  for example: ''null''
 * @param description  for example: ''null''
 * @param date  for example: ''null''
 * @param time  for example: ''null''
 * @param createdAt  for example: ''null''
*/
final case class Time(
  id: Option[Int],
  description: String,
  date: LocalDate,
  time: Int,
  createdAt: Option[OffsetDateTime]
)extends WithId[Time] {
  override def getId: Option[Int] = id
  override def updateId(newId: Option[Int]): Time = copy(id=newId)
}

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

  def getByUser(userId:Int): immutable.Seq[Time] = getList(sql"select * from $table where person=$userId")

  def getByTask(taskId:Int): immutable.Seq[Time] = getList(sql"select * from $table where issue=$taskId")

  def deleteForUser(userId:Int,recordId:Int): Int =
    update(sql"delete from $table where id=$recordId and person=$userId")

  def add(userId:Int,taskId:Int,record:Time): Option[Long] = {
    val time=s"${record.time} hours"
    Try(insert(
      sql"""
        insert into $table (issue,person,time,comment,on_date)
        values(
               $taskId,
               $userId,
               $time,
               ${record.description},
               ${record.date}
        )
         """)).toOptionLogged
  }

}

