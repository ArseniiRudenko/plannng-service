package work.arudenko.kanban.backend.model

import org.postgresql.util.PGInterval
import scalikejdbc._
import work.arudenko.kanban.backend.orm.SqlContext.TryLogged
import scala.util.Try
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
  id: Option[Int] = None,
  header: String,
  description: Option[String] = None,
  priority:String = "low",
  parentId: Option[Int] = None,
  deadline: Option[OffsetDateTime] = None,
  assigneeId: Option[Int] = None,
  estimatedTime: Option[Int] = None,
  tags: Seq[Tag] = Nil,
  status: Option[String] = None)

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
    getList(sql"select * from $table where cur_status=$status::project_track.status")

  def getByTagId(id:Int): Seq[Task] =
    getList(sql"select m.* from $table m  join  project_track.tag_to_issue b on m.id=b.issue_id where b.tag_id=$id")

  def getByTagIds(id:Seq[Int]): Seq[Task] =
    getList(sql"select m.* from $table m  join  project_track.tag_to_issue b on m.id=b.issue_id where b.tag_id in ${id}")

  def getByHeader(header:String): Seq[Task] =DB readOnly { implicit session =>
    getList(sql"select * from $table where header ~ $header")
  }

  def updateStatus(taskId:Int,userId:Int,status:String): Int =
    update(sql"insert into project_track.issue_status_log(status,issue,created_by)  values ($status::project_track.status,$taskId,$userId)")

  def addNew(task:Task,createdBy:Int): Option[Long] =Try(
    DB localTx { implicit session =>
      val id =
        sql"""insert into $table (header,description,priority,parent,deadline,assignee,estimated_time,cur_status,created_by)
             values(${task.header},
                    ${task.description},
                    ${task.priority}::project_track.priority,
                    ${task.parentId},
                    ${task.deadline},
                    ${task.assigneeId},
                    ${task.estimatedTime},
                    ${task.status.getOrElse("backlog")}::project_track.status,
                    ${createdBy})
                    """
      .updateAndReturnGeneratedKey.apply()
      //insert tags into tag to issue table
      sql"insert into project_track.tag_to_issue (tag_id, issue_id) values ({tag}, {issue})"
        .batchByName(
          task.tags.flatMap(tag => tag.id match {
          case Some(value) => Seq(Seq("tag"->value,"issue"->id))
          case None => Nil
        }):_*)
        .apply()
      id
    }).toOptionLogged


  //TODO: allow updating header and description only by creator or admin
  def updateTask(task:Task, updatedBy:Int): Option[Int] = {
    task.id.flatMap(id=>
    Try(update(
      sql"""
        update $table
        set
             header=${task.header},
             description=${task.description},
             priority=${task.priority}::project_track.priority,
             parent=${task.parentId},
             deadline=${task.deadline},
             assignee=${task.assigneeId},
             estimated_time=${task.estimatedTime},
             cur_status=${task.status.getOrElse("backlog")}::project_track.status,
             updated_by=${updatedBy}
        where id=$id
         """)).toOptionLogged
    )
  }

}
