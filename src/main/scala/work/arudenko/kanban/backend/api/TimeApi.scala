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


class TimeApi(
    timeService: TimeApiService,
    timeMarshaller: TimeApiMarshaller
) extends GenericApi {

  
  import timeMarshaller._

  lazy val route: Route =
    path("task" / IntNumber / "time") { (taskId) =>
      authenticateOAuth2("Global", authenticator) {
        implicit auth =>
          post {
            entity(as[Time]) { time =>
              timeService.addTime(taskId = taskId, time = time)
            }
          } ~
            put {
              entity(as[Time]) { time =>
                timeService.updateTime(taskId = taskId, time = time)
              }
            } ~
            get {
              timeService.getTime(taskId = taskId)
            }
      }
    } ~
    path("time" / IntNumber) { (recordId) =>
      authenticateOAuth2("Global", authenticator) {
        implicit auth =>
          delete {
            timeService.deleteTimeRecord(recordId = recordId)
          } ~
            get {
              timeService.getTimeRecordById(recordId = recordId)
            }
      }
    }
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
  implicit def fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time]

  implicit def toEntityMarshallerTime: ToEntityMarshaller[Time]

  implicit def toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]]

  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralResult]

}

