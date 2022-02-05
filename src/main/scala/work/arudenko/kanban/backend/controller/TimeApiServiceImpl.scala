package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.TimeApiService
import work.arudenko.kanban.backend.model.{Comment, GeneralError, Time}

class TimeApiServiceImpl extends TimeApiService  with LazyLogging with AuthenticatedRoute {
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def addTime(taskId: Int, time: Time)(implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  override def deleteTimeRecord(recordId: Int)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      auth =>
            if(auth.user.admin)
              Time.delete(recordId) match {
                case 0 => deleteTimeRecord404
                case _ => deleteTimeRecord200
              }
            else
              Time.deleteForUser(auth.user.id,recordId) match {
                case 0 => deleteTimeRecord404
                case _ => deleteTimeRecord200
              }
    }

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Time]
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def getTime(taskId: Int)(implicit toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
  getTime200(Time.getByTask(taskId))
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  override def getTimeRecordById(recordId: Int)(implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    Time.get(recordId) match {
      case Some(value) => getTimeRecordById200(value)
      case None => getTimeRecordById404
    }
}
