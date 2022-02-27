package work.arudenko.kanban.backend.api
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model.{Project, ProjectCreationInfo, Result}

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
        pathPrefix(IntNumber){ projectNumber=>
          concat(
            pathEndOrSingleSlash{
             concat(
               get{
                 projectService.getProject(projectNumber)
               },
               delete{
                 projectService.deleteProject(projectNumber)
               }
             )
            },
            path("members"){
              concat(
                get{ //get member list
                  ???
                },
                post{ //add memeber
                  ???
                },
                put{ //change member permissions
                  ???
                },
                delete{ //delete member
                  ???
                }
              )
            }
          )
        }
      )
    }


}

trait ProjectApiService{
  def deleteProject(projectNumber: Int)(implicit user:Auth): Result[Unit]

  def getProject(projectNumber: Int)(implicit user:Auth): Result[Project]

  def getProjectList(implicit user:Auth): Result[Seq[Project]]

  def updateProject(project: Project)(implicit user:Auth): Result[Unit]

  def createProject(project: ProjectCreationInfo)(implicit user:Auth): Result[Project]


}

trait ProjectApiMarshaller{

  implicit def fromEntityUnmarshallerProject: FromEntityUnmarshaller[Project]
  implicit def fromEntityUnmarshallerProjectCreationInfo: FromEntityUnmarshaller[ProjectCreationInfo]

  implicit def toEntityMarshallerProject: ToEntityMarshaller[Project]
  implicit def toEntityMarshallerProjectSeq: ToEntityMarshaller[Seq[Project]]
}
