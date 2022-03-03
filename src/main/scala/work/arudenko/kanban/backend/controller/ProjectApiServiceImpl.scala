package work.arudenko.kanban.backend.controller

import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.ProjectApiService
import work.arudenko.kanban.backend.model._

object ProjectApiServiceImpl extends ProjectApiService with LazyLogging{

  private val intToResult:Int=>Result[Unit] = {
    case 0 => NotFound
    case 1 => SuccessEmpty
  }

  private def wrap[T]:T=>Result[T] = r=> SuccessEntity(r)

  private def processMemershipRequest[T,R](
                                            projectId:Int,
                                            modelChange:()=>T,
                                            resultConvert:T=>Result[R])
                                          (implicit auth: Auth) =
    auth.user.projects.find(p => p.projectId == projectId && p.canManageMembers) match {
      case Some(_) => resultConvert(modelChange())
      case None => NotAuthorized
    }


  override def deleteMember(membership: Membership)(implicit auth: Auth): Result[Unit] = {
    processMemershipRequest(
      membership.projectId,
      ()=>Membership.delete(membership.projectId,membership.memberId),
      intToResult
    )
  }

  override def updateMember(membership: Membership)(implicit user: Auth): Result[Unit] = ???


  override def inviteMember(membership: Membership)(implicit auth: Auth): Result[Unit] = {
    processMemershipRequest(
      membership.projectId,
      ()=>Membership.invite(membership),
      intToResult
    )
  }

  override def requestAccess(projectNumber: Int)(implicit auth: Auth): Result[Unit] = {
    processMemershipRequest(
      projectNumber,
      ()=>Membership.request(auth.user.id,projectNumber),
      intToResult
    )
  }

  override def getMembers(projectNumber: Int)(implicit user: Auth): Result[Seq[MembershipInfo]] =
    processMemershipRequest(
      projectNumber,
      ()=>Membership.getProjectInfoListForProject(projectNumber),
      wrap[Seq[MembershipInfo]]
    )

  override def deleteProject(projectNumber: Int)(implicit user: Auth): Result[Unit] = ???

  override def getProject(projectNumber: Int)(implicit user: Auth): Result[Project] = ???

  override def getProjectList(implicit user: Auth): Result[Seq[Project]] = ???

  override def updateProject(project: Project)(implicit user: Auth): Result[Unit] = ???

  override def createProject(project: ProjectCreationInfo)(implicit user: Auth): Result[Project] = ???

  override def rejectAccess(projectNumber: Int)(implicit user: Auth): Result[Unit] = ???
}
