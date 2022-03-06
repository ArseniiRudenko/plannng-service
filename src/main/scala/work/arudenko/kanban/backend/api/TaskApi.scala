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
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.{FileField, MultipartDirectives, StringDirectives}
import work.arudenko.kanban.backend.model.{GeneralResult, Result, Task, Time}


class TaskApi(
    taskService: TaskApiService,
    taskMarshaller: TaskApiMarshaller
)  extends MultipartDirectives with StringDirectives with AuthenticatedApi {

  
  import taskMarshaller._

  override def route(implicit auth: Auth): Route =
    pathPrefix("task") {
        concat(
          pathEndOrSingleSlash{
            concat(
              post {
                entity(as[Task]) { task =>
                  taskService.addTask(task = task)
                }
              },
              put {
                entity(as[Task]) { task =>
                  taskService.updateTask(task = task)
                }
              }
            )
          },
          pathPrefix(IntNumber) { taskId =>
            concat(
              pathEndOrSingleSlash {
                concat(
                  delete {
                    taskService.deleteTask(taskId = taskId)
                  },
                  get {
                    taskService.getTaskById(taskId = taskId)
                  }
                )
              },
              path("status" / Segment) { status =>
                put {
                  taskService.updateTaskStatus(taskId = taskId, status = status)
                }
              },
              path("uploadImage") {
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
            )
          },
          path("findByStatus") {
            post {
              entity(as[String]) { status =>
                taskService.findTaskByStatus(status = status)
              }
            }
          },
          path("findByTags") {
            post {
              entity(as[String]) { tags =>
                taskService.findTasksByTags(tags = tags)
              }
            }
          }
        )
    }


}


trait TaskApiService {

  /**
   * Code: 200, Message: successful task operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def addTask(task: Task)(implicit auth: Auth):Result[Task]

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def deleteTask(taskId: Int)(implicit auth: Auth):Result[Task]

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def findTaskByStatus(status: String)(implicit auth: Auth):Result[Seq[Task]]

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Task]
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def findTasksByTags(tags: String)(implicit auth: Auth):Result[Seq[Task]]

  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def getTaskById(taskId: Int)(implicit auth: Auth):Result[Task]

  /**
   * Code: 200, Message: successful operation, DataType: Task
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def updateTask(task: Task)(implicit auth: Auth):Result[Task]

  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   * Code: 200, Message: successful operation
   */
  def updateTaskStatus(taskId: Int, status: String)(implicit auth: Auth):Result[Task]

  /**
   * Code: 200, Message: successful operation
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: task not found
   */
  def uploadFile(taskId: Int, file: (FileInfo, File))
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]): Route

}

trait TaskApiMarshaller {
  implicit val fromEntityUnmarshallerTask: FromEntityUnmarshaller[Task]

  implicit val toEntityMarshallerTaskarray: ToEntityMarshaller[Seq[Task]]

  implicit val toEntityMarshallerTask: ToEntityMarshaller[Task]

  implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]

}

