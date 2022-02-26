package work.arudenko.kanban.backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import work.arudenko.kanban.backend.api._
import work.arudenko.kanban.backend.controller._
import work.arudenko.kanban.backend.serialization._
import work.arudenko.kanban.backend.serialization.shitty._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import work.arudenko.kanban.backend.orm.SqlContext

object Main extends GenericApi {

  def main(args:Array[String]): Unit ={
    implicit val actorSystem: ActorSystem = ActorSystem("BackendActorSystem")
    val commentApi = new CommentApi(CommentApiServiceImpl,CommentApiMarshallerImpl)
    val taskApi = new TaskApi(TaskApiServiceImpl,TaskApiMarshallerImpl)
    val timeApi = new TimeApi(TimeApiServiceImpl,TimeApiMarshallerImpl)
    val userApi = new UserApi(new UserApiServiceImpl,UserApiMarshallerImpl)
    val adminUserApi = new AdminUserApi(AdminUserApiServiceImpl,AdminUserApiMarshallerImpl)

    val routes: Route = {
      concat(
        userApi.loginRoute,
        authenticateOAuth2("Global", authenticator) {
          implicit auth =>
            concat(
              commentApi.route,
              taskApi.route,
              timeApi.route,
              userApi.route
            )
        },
        authenticateOAuth2("Global", adminAuthenticator) {
          implicit auth => adminUserApi.route
        }
      )
    }

    Http().newServerAt("0.0.0.0", 9000).bindFlow(routes)
  }

}
