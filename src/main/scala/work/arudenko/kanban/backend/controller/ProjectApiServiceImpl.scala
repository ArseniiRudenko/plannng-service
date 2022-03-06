package work.arudenko.kanban.backend.controller

import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.ProjectApiService
import work.arudenko.kanban.backend.model._

object ProjectApiServiceImpl extends ProjectApiService with LazyLogging{

  private val intToResult:Int=>Result[Unit] = {
    case 0 => NotFound
    case 1 => SuccessEmpty
  }

  private def processMemershipRequest[T,R](
                                            projectId:Int,
                                            modelChange:()=>T,
                                            resultConvert:T=>Result[R])
                                          (implicit auth: Auth): Result[R] =
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

  override def updateMember(membership: Membership)(implicit user: Auth): Result[Unit] ={
    processMemershipRequest(
      membership.projectId,
      ()=>Membership.updateMember(membership),
      intToResult
    )
  }

  override def inviteMember(membershipInfo: MembershipInfo)(implicit auth: Auth): Result[Unit] = {
    processMemershipRequest(
      membershipInfo.projectId,
      ()=> membershipInfo.member.id match {
        case Some(value) =>
          val membership = Membership(membershipInfo.projectId,value,membershipInfo.canManageMembers,membershipInfo.canManageTasks)
          intToResult.apply(Membership.invite(membership))
        case None =>
          val users = User.searchUser(membershipInfo.member,Some(2))
          users.length match {
            case 1=>
              val membership = Membership(membershipInfo.projectId,users.head.id,membershipInfo.canManageMembers,membershipInfo.canManageTasks)
              intToResult.apply(Membership.invite(membership))
            case 0=> WrongInput("no users match the criteria")
            case _=> WrongInput("more than one user matches the criteria, please refine the info")
          }
      },
      identity[Result[Unit]]
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
      SuccessEntity[Seq[MembershipInfo]]
    )


  override def deleteProject(projectNumber: Int)(implicit auth: Auth): Result[Unit] =
          Project.delete(projectNumber,auth.user.id) match {
            case 1 => SuccessEmpty
            case 0 => NotFound
            case n =>
              logger.error(s"errror removing project $projectNumber by user ${auth.user}, num records $n")
              GeneralResult(500,"db request error")
          }



  override def getProject(projectNumber: Int)(implicit user: Auth): Result[Project] =
    Project.get(projectNumber) match {
      case Some(value) => SuccessEntity(value)
      case None => NotFound
    }

  override def getProjectList(implicit auth: Auth): Result[Seq[Project]] =
    SuccessEntity(Project.getProjectList(auth.user.projects.map(_.projectId).toSeq))

  override def updateProject(project: Project)(implicit auth: Auth): Result[Unit] =
    intToResult(Project.updateProj(project,auth.user.id))


  override def createProject(project: ProjectCreationInfo)(implicit auth: Auth): Result[Project] =
    Project.create(project,auth.user.id) match {
      case Some(value) => SuccessEntity(value)
      case None => WrongInput("something went wrong")
    }


  override def rejectInvite(projectNumber: Int)(implicit auth: Auth): Result[Unit] =
    intToResult.apply(Membership.delete(projectNumber,auth.user.id))

  override def transferOwnership(projectNumber: Int, userInfo: UserInfo)(implicit auth: Auth): Result[Unit] = {
    userInfo.id match {
      case Some(value) => intToResult.apply(Project.transfer(projectNumber:Int,auth.user.id,value))
      case None =>
        val users = User.searchUser(userInfo,Some(2))
        users.length match {
          case 1=> intToResult.apply(Project.transfer(projectNumber,auth.user.id,users.head.id))
          case 0=> WrongInput("no users match the criteria")
          case _=> WrongInput("more than one user matches the criteria, please refine the info")
        }

    }
  }
}
