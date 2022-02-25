package work.arudenko.kanban.backend.model

import scalikejdbc.WrappedResultSet
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations
import scalikejdbc._


final case class Project(id:Int,name:String, description:Option[String],owner:Int)

object Project  extends WithCommonSqlOperations[Project] {

  override val tableName = "project_track.projects"

  override def sqlExtractor(rs: WrappedResultSet): Project = Project(
    rs.int("id"),
    rs.string("name"),
    rs.stringOpt("description"),
    rs.int("owner")
  )

  def getProjectListForUser(userId:Int):Set[Int] =
      DB readOnly { implicit session =>
        sql"select * from project_track.project_membership where person=${userId}"
          .map(rs => rs.int("project"))
          .iterable
          .apply()
          .toSet
      }



}


