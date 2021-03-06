package work.arudenko.kanban.backend

import akka.actor.{ActorSystem, actorRef2Scala}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import work.arudenko.kanban.backend.api._
import work.arudenko.kanban.backend.controller._
import work.arudenko.kanban.backend.serialization.jackson._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor

object Main  {

  def main(args:Array[String]): Unit ={
    implicit val actorSystem: ActorSystem = ActorSystem("BackendActorSystem")
    implicit val context: ExecutionContextExecutor = actorSystem.dispatcher
    val commentApi = new CommentApi(CommentApiServiceImpl,CommentApiMarshallerImpl)
    val taskApi = new TaskApi(TaskApiServiceImpl,TaskApiMarshallerImpl)
    val timeApi = new TimeApi(TimeApiServiceImpl,TimeApiMarshallerImpl)
    val userApi = new UserApi(new UserApiServiceImpl,UserApiMarshallerImpl)
    val adminUserApi = new AdminUserApi(AdminUserApiServiceImpl,AdminUserApiMarshallerImpl)
    val projectApi = new ProjectApi(ProjectApiServiceImpl,ProjectApiMarshallerImpl)


    val routes: Route = {
      concat(
        userApi.loginRoute,
              commentApi.authenticatedRoute,
              taskApi.authenticatedRoute,
              timeApi.authenticatedRoute,
              userApi.authenticatedRoute,
              projectApi.authenticatedRoute,
        adminUserApi.authenticatedRoute
      )
    }

    Http().newServerAt("0.0.0.0", 9000).bindFlow(routes)
  }

}
