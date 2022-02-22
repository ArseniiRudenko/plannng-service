package work.arudenko.kanban.backend

import akka.actor.ActorSystem
import work.arudenko.kanban.backend.api._
import work.arudenko.kanban.backend.controller._
import work.arudenko.kanban.backend.serialization._
import work.arudenko.kanban.backend.serialization.shitty.{CommentApiMarshallerImpl, TaskApiMarshallerImpl, TimeApiMarshallerImpl, UserApiMarshallerImpl}

object Main {

  def main(args:Array[String]): Unit ={
    implicit val actorSystem: ActorSystem = ActorSystem("BackendActorSystem")
    val commentApi = new CommentApi(CommentApiServiceImpl,CommentApiMarshallerImpl)
    val taskApi = new TaskApi(TaskApiServiceImpl,TaskApiMarshallerImpl)
    val timeApi = new TimeApi(TimeApiServiceImpl,TimeApiMarshallerImpl)
    val userApi = new UserApi(new UserApiServiceImpl,UserApiMarshallerImpl)
    var controller = new Controller(commentApi, taskApi, timeApi, userApi)
  }

}
