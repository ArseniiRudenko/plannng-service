package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import work.arudenko.kanban.backend.api.TimeApiService
import work.arudenko.kanban.backend.model.{Comment, GeneralError, Time}

class TimeApiServiceImpl extends TimeApiService  with GenericApi[Time] {
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def addTime(taskId: Int, time: Time)(implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = {
    authenticateOAuth2("Global",authenticator) {
      auth =>
        Time.add(auth.user.id, taskId, record = time) match {
          case Some(value) => timeRecord200(time.copy(id=Some(value.toInt)))
          case None => time400(GeneralError("incorrect value"))
        }
    }
  }

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
                case 0 => time404
                case _ => time200
              }
            else
              Time.deleteForUser(auth.user.id,recordId) match {
                case 0 => time404
                case _ => time200
              }
    }

  /**
   * Code: 200, Message: successful operation, DataType: Seq[Time]
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def getTime(taskId: Int)(implicit toEntityMarshallerTimearray: ToEntityMarshaller[Seq[Time]], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      _ =>
        timeRecordList200(Time.getByTask(taskId))
    }
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  override def getTimeRecordById(recordId: Int)(implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      _ =>
        Time.get(recordId) match {
          case Some(value) => timeRecord200(value)
          case None => time404
        }
    }

  override def updateTime(taskId: Int, time: Time)
                         (implicit toEntityMarshallerTime: ToEntityMarshaller[Time], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      auth =>
        Time.updateForUser(auth.user.id,taskId,time) match {
          case 0 => time404
          case _=> time200
        }
    }

}
