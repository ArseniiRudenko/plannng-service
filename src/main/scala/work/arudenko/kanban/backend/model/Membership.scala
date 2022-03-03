package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

final case class Membership(projectId:Int,memberId:Int,canManageMembers:Boolean,canManageTasks:Boolean)

final case class MembershipInfo(projectId:Int,member:User,canManageMembers:Boolean,canManageTasks:Boolean)

object Membership  extends WithCommonSqlOperations[Membership] {
  def invite(membership: Membership) = update(sql"" +
    sql"""
         insert into $table(person,porject,can_manage_members,can_manage_tasks,is_granted)
         values(${membership.memberId},${membership.projectId},${membership.canManageMembers},${membership.canManageTasks},true)
         on conflict on constraint project_membership_person_project_uindex do
         update set
             can_manage_members=${membership.canManageMembers},
             can_manage_tasks=${membership.canManageTasks},
             is_granted=true
       """)


  override val tableName = "project_track.project_membership"

  override def sqlExtractor(rs: WrappedResultSet): Membership = ???

  def delete(projectNumber: Int, userId: Int): Int =
    update(sql" delete from $table where project=$projectNumber and person=$userId")


  def getProjectListForUser(userId:Int):Set[Membership] =
    DB readOnly { implicit session =>
      sql"select * from $table where person=${userId}"
        .map(rs => sqlExtractor(rs))
        .iterable
        .apply()
        .toSet
    }
}
