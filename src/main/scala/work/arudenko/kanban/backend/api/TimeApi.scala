package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.model.{GeneralResult, Result, Time}
import work.arudenko.kanban.backend.AkkaHttpHelper._
import work.arudenko.kanban.backend.controller.Auth

import scala.concurrent.ExecutionContext


class TimeApi(
    timeService: TimeApiService,
    timeMarshaller: TimeApiMarshaller)
             (implicit ex:ExecutionContext) extends AuthenticatedApi("time") {

  
  import timeMarshaller._

  override def route(implicit auth: Auth): Route =
      concat(
        path("task" / IntNumber) { (taskId) =>
            concat(
                post {
                  entity(as[Time]) { time =>
                    timeService.addTime(taskId = taskId, time = time)
                  }
                },
                put {
                  entity(as[Time]) { time =>
                    timeService.updateTime(taskId = taskId, time = time)
                  }
                },
                get {
                  timeService.getTime(taskId = taskId)
                }
            )
        },
        path(IntNumber) { (recordId) =>
          concat(
            delete {
              timeService.deleteTimeRecord(recordId = recordId)
            },
            get {
              timeService.getTimeRecordById(recordId = recordId)
            }
          )
        }
      )
}


trait TimeApiService {
  def updateTime(taskId: Int, time: Time)(implicit auth: Auth):Result[Time]

  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def addTime(taskId: Int, time: Time)(implicit auth: Auth):Result[Time]
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  def deleteTimeRecord(recordId: Int)(implicit auth: Auth):Result[Time]

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Time]
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def getTime(taskId: Int)(implicit auth: Auth):Result[Seq[Time]]

  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  def getTimeRecordById(recordId: Int)(implicit auth: Auth):Result[Time]

}

trait TimeApiMarshaller {
  implicit val fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time]

  implicit val toEntityMarshallerTime: ToEntityMarshaller[Time]

  implicit val toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]]

  implicit val toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]

}

