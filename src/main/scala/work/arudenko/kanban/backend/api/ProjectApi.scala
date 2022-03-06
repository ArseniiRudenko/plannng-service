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
            concat(
              post {
                  //add memeber
                entity(as[MembershipInfo]) { membershipInfo =>
                  projectService.inviteMember(membershipInfo)
                }
              },
              put {
                entity(as[Membership]) { membership =>
                  //change member permissions
                  projectService.updateMember(membership)
                }
              },
              delete {
                entity(as[Membership]) { membership =>
                  projectService.deleteMember(membership)
                }
              }
            )
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

  def inviteMember(membership: MembershipInfo)(implicit user:Auth): Result[Unit]

  def getMembers(projectNumber: Int)(implicit user:Auth): Result[Seq[MembershipInfo]]

  def deleteProject(projectNumber: Int)(implicit user:Auth): Result[Unit]

  def getProject(projectNumber: Int)(implicit user:Auth): Result[Project]

  def getProjectList(implicit user:Auth): Result[Seq[Project]]

  def updateProject(project: Project)(implicit user:Auth): Result[Unit]

  def createProject(project: ProjectCreationInfo)(implicit user:Auth): Result[Project]


}

trait ProjectApiMarshaller{
  implicit val fromEntityUnmarshallerInt: FromEntityUnmarshaller[Int]

  implicit val fromEntityUnmarshallerProject: FromEntityUnmarshaller[Project]

  implicit val fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo]

  implicit val fromEntityUnmarshallerMembership: FromEntityUnmarshaller[Membership]

  implicit val fromEntityUnmarshallerMembershipInfo: FromEntityUnmarshaller[MembershipInfo]

  implicit val fromEntityUnmarshallerProjectCreationInfo: FromEntityUnmarshaller[ProjectCreationInfo]

  implicit val toEntityMarshallerProject: ToEntityMarshaller[Project]

  implicit val toEntityMarshallerMembershipInfo: ToEntityMarshaller[MembershipInfo]

  implicit def toEntityMarshallerMembershipInfoSeq: ToEntityMarshaller[Seq[MembershipInfo]]

  implicit def toEntityMarshallerProjectSeq: ToEntityMarshaller[Seq[Project]]
}
