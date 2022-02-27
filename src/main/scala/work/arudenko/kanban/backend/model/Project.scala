package work.arudenko.kanban.backend.model

import scalikejdbc.WrappedResultSet
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations
import scalikejdbc._

final case class ProjectCreationInfo(name:String, description:Option[String])

final case class Project(id:Int,name:String, description:Option[String],owner:Int)

object Project  extends WithCommonSqlOperations[Project] {

  override val tableName = "project_track.projects"

  override def sqlExtractor(rs: WrappedResultSet): Project = Project(
    rs.int("id"),
    rs.string("name"),
    rs.stringOpt("description"),
    rs.int("owner")
  )





}


