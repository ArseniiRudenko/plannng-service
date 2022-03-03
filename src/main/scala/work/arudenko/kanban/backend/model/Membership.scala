package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

final case class Membership(projectId:Int,memberId:Int,canManageMembers:Boolean,canManageTasks:Boolean)

final case class MembershipInfo(projectId:Int,member:User,canManageMembers:Boolean,canManageTasks:Boolean)

object Membership  extends WithCommonSqlOperations[Membership] {

  def invite(membership: Membership): Int =
    update(
    sql"""
         insert into $table(person,project,can_manage_members,can_manage_tasks,is_granted)
         values(${membership.memberId},${membership.projectId},${membership.canManageMembers},${membership.canManageTasks},true)
         on conflict on constraint project_membership_person_project_uindex do
         update set
             can_manage_members=${membership.canManageMembers},
             can_manage_tasks=${membership.canManageTasks},
             is_granted=true
       """
    )

  def request(userId:Int,projectNumber:Int): Int =
    update(
      sql"""
         insert into $table(person,project,is_accepted)
         values($userId,$projectNumber,true)
         on conflict on constraint project_membership_person_project_uindex do
         update set
             is_accepted=true
       """
    )


  override val tableName = "project_track.project_membership"

  override def sqlExtractor(rs: WrappedResultSet): Membership =
    new Membership(
    rs.int("project"),
    rs.int("person"),
    rs.boolean("can_manage_members"),
    rs.boolean("can_manage_tasks")
  )


  def infoSqlExtractor(rs: WrappedResultSet): MembershipInfo = new MembershipInfo(
    rs.int("project"),
    User.sqlExtractor(rs),
    rs.boolean("can_manage_members"),
    rs.boolean("can_manage_tasks")
  )

  def delete(projectNumber: Int, userId: Int): Int =
    update(sql" delete from $table where project=$projectNumber and person=$userId")



  def getProjectInfoListForUser(userId:Int):Set[MembershipInfo] =
    DB readOnly { implicit session =>
      sql"select * from $table join ${User.table} on $table.person=${User.table}.id where person=$userId"
        .map(rs => infoSqlExtractor(rs))
        .iterable
        .apply()
        .toSet
    }

  def getProjectInfoListForProject(projectId:Int):Seq[MembershipInfo] =
    DB readOnly { implicit session =>
      sql"select * from $table join ${User.table} on $table.person=${User.table}.id where project=$projectId"
        .map(rs => infoSqlExtractor(rs))
        .list
        .apply()
    }


  def getProjectListForUser(userId:Int):Set[Membership] =
    DB readOnly { implicit session =>
      sql"select * from $table where person=$userId"
        .map(rs => sqlExtractor(rs))
        .iterable
        .apply()
        .toSet
    }
}
