package work.arudenko.kanban.backend.api
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model.{Project, ProjectCreationInfo}

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
                 ???
               }
             },
             put { //update project
               entity(as[Project]) { project =>
                  ???
               }
             },
             get{ //get list of all your projects
                 ???
             }
           )
        },
        pathPrefix(IntNumber){ projectNumber=>
          concat(
            pathEndOrSingleSlash{
             concat(
               get{
                 ???
               },
               delete{
                 ???
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

trait ProjectApiService

trait ProjectApiMarshaller{



}
