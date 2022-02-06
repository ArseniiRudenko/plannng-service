package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.TaskApiService
import work.arudenko.kanban.backend.model.{GeneralError, Tag, Task}

import java.io.File

class TaskApiServiceImpl extends TaskApiService with LazyLogging with AuthenticatedRoute{
  /**
   * Code: 200, Message: successful task operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def addTask(task: Task)(implicit toEntityMarshallerTask: ToEntityMarshaller[Task], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def deleteTask(taskId: Int)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      auth =>
        if (auth.user.admin)
          Task.delete(taskId) match {
            case 0 => Task404
            case _ => Task200
          }
        else
          Task.delete(taskId) match {
            case 0 => Task404
            case _ => Task200
          }
    }

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def findTaskByStatus(status: String)(implicit toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      _ =>
        findTaskByStatus200(Task.getByStatus(status))
    }
  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def findTasksByTags(tags: String)(implicit toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      _ =>
        findTasksByTags200(Task.getByTagIds(Tag.getTagsByName(tags).map(tag => tag.id.get)))
    }
  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def getTaskById(taskId: Int)(implicit toEntityMarshallerTask: ToEntityMarshaller[Task], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      _ =>
        Task.get(taskId) match {
          case Some(value) => getTaskById200(value)
          case None => Task404
        }
    }


  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def updateTask(task: Task)(implicit toEntityMarshallerTask: ToEntityMarshaller[Task], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   * Code: 200, Message: successful operation
   */
  override def updateTaskStatus(taskId: Int, status: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      auth =>
            Task.updateStatus(taskId,auth.user.id,status) match {
              case 0 => Task404
              case _ => Task200
            }
    }

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   */
  override def uploadFile(taskId: Int, file: (FileInfo, File))(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???
}
