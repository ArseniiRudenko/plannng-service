package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import work.arudenko.kanban.backend.model.{GeneralError, Time}
import work.arudenko.kanban.backend.AkkaHttpHelper._
import work.arudenko.kanban.backend.model.GeneralError


class TimeApi(
    timeService: TimeApiService,
    timeMarshaller: TimeApiMarshaller
) {

  
  import timeMarshaller._

  lazy val route: Route =
    path("task" / IntNumber / "time") { (taskId) => 
      post {  
            entity(as[Time]){ time =>
              timeService.addTime(taskId = taskId, time = time)
            }
      } ~
      put {
          entity(as[Time]){ time =>
            timeService.updateTime(taskId = taskId, time = time)
          }
      } ~
      get {
        timeService.getTime(taskId = taskId)
      }
    } ~
    path("time" / IntNumber) { (recordId) => 
      delete {  
            timeService.deleteTimeRecord(recordId = recordId)
      }~
      get {
          timeService.getTimeRecordById(recordId = recordId)
      }
    }
}


trait TimeApiService {
  def updateTime(taskId: Int, time: Time)
                (implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  def timeRecord200(responseTime: Time)(implicit toEntityMarshallerTime: ToEntityMarshaller[Time]): Route =
    complete((200, responseTime))

  def timeRecordList200(responseTime: Seq[Time])(implicit toEntityMarshallerTime: ToEntityMarshaller[Seq[Time]]): Route =
    complete((200, responseTime))

  def time400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))
  val time404: Route =
    complete((404, "Task or record not found"))
  val time200: Route =
    complete((200, "Success"))
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  def addTime(taskId: Int, time: Time)
      (implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  def deleteTimeRecord(recordId: Int)
      (implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Time]
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  def getTime(taskId: Int)
      (implicit toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  def getTimeRecordById(recordId: Int)
      (implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route

}

trait TimeApiMarshaller {
  implicit def fromEntityUnmarshallerTime: FromEntityUnmarshaller[Time]

  implicit def toEntityMarshallerTime: ToEntityMarshaller[Time]

  implicit def toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]]

  implicit def toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]

}

