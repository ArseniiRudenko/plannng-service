package work.arudenko.kanban.backend.controller

import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.ProjectApiService
import work.arudenko.kanban.backend.model._

object ProjectApiServiceImpl extends ProjectApiService with LazyLogging{

  override def deleteMember(membership: Membership)(implicit auth: Auth): Result[Unit] = {
    auth.user.projects.find(p => p.projectId == membership.projectId && p.canManageMembers) match {
      case Some(value) => Membership.delete(membership.projectId,membership.memberId) match {
        case 0 => NotFound
        case 1 => SuccessEmpty
      }
      case None => NotAuthorized
    }
  }

  override def updateMember(membership: Membership)(implicit user: Auth): Result[Unit] = ???



  override def inviteMember(membership: Membership)(implicit auth: Auth): Result[Unit] = {
    auth.user.projects.find(p => p.projectId == membership.projectId && p.canManageMembers) match {
      case Some(value) => Membership.invite(membership) match {
        case 0 => NotFound
        case 1 => SuccessEmpty
      }
      case None => NotAuthorized
    }
  }

  override def requestAccess(projectNumber: Int)(implicit auth: Auth): Result[Unit] = {
    auth.user.projects.find(p => p.projectId == projectNumber && p.canManageMembers) match {
      case Some(value) => Membership.request(auth.user.id,projectNumber) match {
        case 0 => NotFound
        case 1 => SuccessEmpty
      }
      case None => NotAuthorized
    }
  }

  override def getMembers(projectNumber: Int)(implicit user: Auth): Result[MembershipInfo] = ???

  override def deleteProject(projectNumber: Int)(implicit user: Auth): Result[Unit] = ???

  override def getProject(projectNumber: Int)(implicit user: Auth): Result[Project] = ???

  override def getProjectList(implicit user: Auth): Result[Seq[Project]] = ???

  override def updateProject(project: Project)(implicit user: Auth): Result[Unit] = ???

  override def createProject(project: ProjectCreationInfo)(implicit user: Auth): Result[Project] = ???



  override def rejectAccess(projectNumber: Int)(implicit user: Auth): Result[Unit] = ???
}
