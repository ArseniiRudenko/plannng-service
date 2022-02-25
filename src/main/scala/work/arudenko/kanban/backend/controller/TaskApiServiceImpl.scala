package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.TaskApiService
import work.arudenko.kanban.backend.model.{GeneralResult, NotFound, Result, SuccessEmpty, SuccessEntity, Tag, Task, WrongInput}

import java.io.File

object TaskApiServiceImpl extends TaskApiService with LazyLogging{
  /**
   * Code: 200, Message: successful task operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def addTask(task: Task)(implicit auth: Auth):Result[Task]=
        Task.addNew(task, auth.user.id) match {
          case Some(value) => SuccessEntity(task.copy(id=Some(value.toInt)))
          case None => WrongInput("incorrect task parameters")
        }
  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def deleteTask(taskId: Int)(implicit auth: Auth):Result[Task] =
        if (auth.user.admin)
          Task.delete(taskId) match {
            case 0 => NotFound
            case _ => SuccessEmpty
          }
        else
          Task.delete(taskId) match {
            case 0 => NotFound
            case _ => SuccessEmpty
          }


  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def findTaskByStatus(status: String)(implicit auth: Auth):Result[Seq[Task]] =
        SuccessEntity(Task.getByStatus(status))
    /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def findTasksByTags(tags: String)(implicit auth: Auth):Result[Seq[Task]] =
        SuccessEntity(Task.getByTagIds(Tag.getTagsByName(tags).map(tag => tag.id.get)))

  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def getTaskById(taskId: Int)(implicit auth: Auth):Result[Task] =

        Task.get(taskId) match {
          case Some(value) => SuccessEntity(value)
          case None => NotFound
        }



  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def updateTask(task: Task)(implicit auth: Auth):Result[Task] =

        Task.updateTask(task, auth.user) match {
          case Some(value) => value match {
            case 1 => SuccessEntity(task)
            case 0 => NotFound
            case v =>
              logger.error(s" unexpected number of updated tasks $v when updating task $task by user $auth.user")
              WrongInput("task update gone wrong, contact support")
          }
          case None => WrongInput("incorrect update request")
        }


  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   * Code: 200, Message: successful operation
   */
  override def updateTaskStatus(taskId: Int, status: String)(implicit auth: Auth):Result[Task] =
    Task.updateStatus(taskId,auth.user.id,status) match {
      case 0 => NotFound
      case _ => SuccessEmpty
    }


  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   */
  override def uploadFile(taskId: Int, file: (FileInfo, File))(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]): Route = ???
}
