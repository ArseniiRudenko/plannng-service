package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.AkkaHttpHelper._
import java.io.File
import scala.util.Try
import akka.http.scaladsl.server.MalformedRequestContentRejection
import akka.http.scaladsl.server.directives.FileInfo
import work.arudenko.kanban.backend.{FileField, MultipartDirectives, StringDirectives}
import work.arudenko.kanban.backend.model.{GeneralError, Task}


class TaskApi(
    taskService: TaskApiService,
    taskMarshaller: TaskApiMarshaller
)  extends MultipartDirectives with StringDirectives {

  
  import taskMarshaller._

  lazy val route: Route =
    path("task") { 
      post {  
        entity(as[Task]){ task =>
          taskService.addTask(task = task)
        }
      } ~
      put {
        entity(as[Task]) { task =>
          taskService.updateTask(task = task)
        }
      }
    } ~
    path("task" / IntNumber) { (taskId) => 
      delete {  
        taskService.deleteTask(taskId = taskId)
      }~
      get {
        taskService.getTaskById(taskId = taskId)
      }
    } ~
    path("task" / "findByStatus") { 
      get { 
        parameters("status".as[String]) { (status) => 
            taskService.findTaskByStatus(status = status)
        }
      }
    } ~
    path("task" / "findByTags") { 
      get { 
        parameters("tags".as[String]) { (tags) => 
            taskService.findTasksByTags(tags = tags)
        }
      }
    } ~
    path("task" / IntNumber / "status" / Segment) { (taskId, status) => 
      put {  
            taskService.updateTaskStatus(taskId = taskId, status = status)
      }
    } ~
    path("task" / IntNumber / "uploadImage") { (taskId) => 
      post {  
        formAndFiles(FileField("file")) { partsAndFiles => 
          val rt: Try[Route] = for {
              file <- optToTry(partsAndFiles.files.get("file"), s"File file missing")
            } yield {
              implicit val vp: StringValueProvider = partsAndFiles.form
                taskService.uploadFile(taskId = taskId, file = file)
            }
            rt.fold[Route](t => reject(MalformedRequestContentRejection("Missing file.", t)), identity)
          }
        }
      }
}


trait TaskApiService {

  def taskRecord200(responseTask: Task)(implicit toEntityMarshallerTask: ToEntityMarshaller[Task]): Route =
    complete((200, responseTask))
  def task400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  def taskArray200(responseTaskArray: Seq[Task])(implicit toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]]): Route =
    complete((200, responseTaskArray))
  val Task404: Route =
    complete((404, "Task not found"))
  val Task200: Route =
    complete((200, "successful operation"))

  /**
   * Code: 200, Message: successful task operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def addTask(task: Task)
      (implicit toEntityMarshallerTask: ToEntityMarshaller[Task], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def deleteTask(taskId: Int)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def findTaskByStatus(status: String)
      (implicit toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def findTasksByTags(tags: String)
      (implicit toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def getTaskById(taskId: Int)
      (implicit toEntityMarshallerTask: ToEntityMarshaller[Task], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def updateTask(task: Task)
      (implicit toEntityMarshallerTask: ToEntityMarshaller[Task], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   * Code: 200, Message: successful operation
   */
  def updateTaskStatus(taskId: Int, status: String)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   */
  def uploadFile(taskId: Int, file: (FileInfo, File))
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

}

trait TaskApiMarshaller {
  implicit def fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task]

  implicit def toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]]

  implicit def toEntityMarshallerTask: ToEntityMarshaller[Task]

  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]

}

