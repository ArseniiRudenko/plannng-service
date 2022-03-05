package work.arudenko.kanban.backend.api
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model.{Membership, MembershipInfo, Project, ProjectCreationInfo, Result, UserInfo}

class ProjectApi (
                   projectService: ProjectApiService,
                   projectMarshaller: ProjectApiMarshaller
                 )  extends AuthenticatedApi {

  import projectMarshaller._

  override def route(implicit auth: Auth): Route =
    pathPrefix("project"){
      concat(
        pathEndOrSingleSlash{
          concat(
             post {//create project

               //TODO: add endpoint and logic for transferring project ownership
               entity(as[ProjectCreationInfo]) { project=>
                 projectService.createProject(project)
               }
             },
             put { //update project
               entity(as[Project]) { project =>
                  projectService.updateProject(project)
               }
             },
             get{ //get list of all your projects
                 projectService.getProjectList
             }
           )
        },
        path("member"){
          entity(as[Membership]) { membership =>
            concat(
              post {
                  //add memeber
                //TODO: allow inviting users by email
                  projectService.inviteMember(membership)
              },
              put {
                  //change member permissions
                  projectService.updateMember(membership)
              },
              delete {
                  projectService.deleteMember(membership)
              }
            )
          }
        },
        pathPrefix(IntNumber){ projectNumber=>
          concat(
            pathEndOrSingleSlash{
             concat(
               get{
                 projectService.getProject(projectNumber)
               },
               delete{
                 projectService.deleteProject(projectNumber)
               },
               post{
                 //transfer project to
                  entity(as[UserInfo]){userInfo=>
                    projectService.transferOwnership(projectNumber,userInfo)
                  }
               }
             )
            },
            path("apply"){
              concat(
                post{//accept invite or request access to project
                  projectService.requestAccess(projectNumber)
                },
                delete{ //reject invite or cancel request
                  projectService.rejectInvite(projectNumber)
                }
              )
            },
            path("members"){
              concat(
                get{ //get member list
                  projectService.getMembers(projectNumber)
                }
              )
            }
          )
        }
      )
    }


}

trait ProjectApiService{
  def transferOwnership(projectNumber: Int, userInfo: UserInfo)(implicit auth:Auth): Result[Unit]

  def requestAccess(projectNumber: Int)(implicit user:Auth): Result[Unit]

  def rejectInvite(projectNumber: Int)(implicit user:Auth): Result[Unit]

  def deleteMember(membership: Membership)(implicit user:Auth): Result[Unit]

  def updateMember(membership: Membership)(implicit user:Auth): Result[Unit]

  def inviteMember(membership: Membership)(implicit user:Auth): Result[Unit]

  def getMembers(projectNumber: Int)(implicit user:Auth): Result[Seq[MembershipInfo]]

  def deleteProject(projectNumber: Int)(implicit user:Auth): Result[Unit]

  def getProject(projectNumber: Int)(implicit user:Auth): Result[Project]

  def getProjectList(implicit user:Auth): Result[Seq[Project]]

  def updateProject(project: Project)(implicit user:Auth): Result[Unit]

  def createProject(project: ProjectCreationInfo)(implicit user:Auth): Result[Project]


}

trait ProjectApiMarshaller{
  implicit def fromEntityUnmarshallerInt: FromEntityUnmarshaller[Int]

  implicit def fromEntityUnmarshallerProject: FromEntityUnmarshaller[Project]

  implicit def fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo]

  implicit def fromEntityUnmarshallerMembership: FromEntityUnmarshaller[Membership]

  implicit def fromEntityUnmarshallerProjectCreationInfo: FromEntityUnmarshaller[ProjectCreationInfo]

  implicit def toEntityMarshallerProject: ToEntityMarshaller[Project]

  implicit def toEntityMarshallerMembershipInfo: ToEntityMarshaller[MembershipInfo]

  implicit def toEntityMarshallerMembershipInfoSeq: ToEntityMarshaller[Seq[MembershipInfo]]

  implicit def toEntityMarshallerProjectSeq: ToEntityMarshaller[Seq[Project]]
}
