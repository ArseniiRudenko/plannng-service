package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

final case class Membership(projectId:Int,memberId:Int,canManageMembers:Boolean,canManageTasks:Boolean)

final case class MembershipInfo(projectId:Int,member:User,canManageMembers:Boolean,canManageTasks:Boolean)

object Membership  extends WithCommonSqlOperations[Membership] {
  override val tableName = "project_track.project_membership"

  override def sqlExtractor(rs: WrappedResultSet): Membership = ???


  def getProjectListForUser(userId:Int):Set[Int] =
    DB readOnly { implicit session =>
      sql"select * from $table where person=${userId}"
        .map(rs => rs.int("project"))
        .iterable
        .apply()
        .toSet
    }
}
