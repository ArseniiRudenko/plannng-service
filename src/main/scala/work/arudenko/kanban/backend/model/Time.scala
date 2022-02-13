package work.arudenko.kanban.backend.model

import org.postgresql.util.PGInterval
import scalikejdbc._
import work.arudenko.kanban.backend.orm.SqlContext.TryLogged
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import java.time.{LocalDate, LocalTime, OffsetDateTime}
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
                       time: LocalTime,
                       createdAt: Option[OffsetDateTime]
){

  def getTimeAsInterval:PGInterval = {
    new PGInterval(0,0,0,time.getHour,time.getMinute,time.getSecond)
  }

}

object Time extends WithCommonSqlOperations[Time]{
  override def sqlExtractor(rs: WrappedResultSet): Time =
    new Time(
      Some(rs.int("id")),
      rs.string("comment"),
      rs.localDate("on_date"),
      rs.get[PGInterval]("time"),
      Some(rs.offsetDateTime("created_at"))
    )

  private implicit def intervalToLocalTime(interval:PGInterval):LocalTime = {
    LocalTime.of(interval.getHours,interval.getMinutes,interval.getWholeSeconds)
  }


  override val tableName = "project_track.spent_time"

  def getByUser(userId:Int): immutable.Seq[Time] = getList(sql"select * from $table where person=$userId")

  def getByTask(taskId:Int): immutable.Seq[Time] = getList(sql"select * from $table where issue=$taskId")

  def deleteForUser(userId:Int,recordId:Int): Int =
    update(sql"delete from $table where id=$recordId and person=$userId")

  def add(userId:Int,taskId:Int,record:Time): Option[Long] = {
    Try(insert(
      sql"""
        insert into $table (issue,person,time,comment,on_date)
        values(
               $taskId,
               $userId,
               ${record.getTimeAsInterval},
               ${record.description},
               ${record.date}
        )
         """)).toOptionLogged
  }

  def updateForUser(userId:Int,taskId:Int,record:Time): Int =
    update(
      sql"""update $table
           set
            comment=${record.description},
            on_date=${record.date},
            time=${record.getTimeAsInterval},
            issue=${taskId}
            where id=${record.id} and person=${userId}""")


}

