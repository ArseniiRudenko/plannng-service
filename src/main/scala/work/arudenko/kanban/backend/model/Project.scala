package work.arudenko.kanban.backend.model

import scalikejdbc.WrappedResultSet
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations
import scalikejdbc._
import work.arudenko.kanban.backend.orm.SqlContext.TryLogged

import scala.util.Try

final case class ProjectCreationInfo(name:String, description:Option[String])

final case class Project(id:Int,name:String, description:Option[String],owner:Int)

object Project  extends WithCommonSqlOperations[Project] {
  def create(project: ProjectCreationInfo,userId:Int): Option[Project] = Try{
      val id=insert(
      sql"""
        insert into $table(name,description,owner)
        values(${project.name},${project.description},${userId})
         """)
    new Project(id.toInt,project.name,project.description,userId)
  }.toOptionLogInfo


  override val tableName = "project_track.projects"

  override def sqlExtractor(rs: WrappedResultSet): Project = Project(
    rs.int("id"),
    rs.string("name"),
    rs.stringOpt("description"),
    rs.int("owner")
  )

  def getProjectList(ids:Seq[Int]): Seq[Project] = getList(sql"select * from  $table where id in ($ids)")


}


