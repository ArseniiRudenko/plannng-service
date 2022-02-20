package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.authenticateOAuth2
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.api.TimeApiService
import work.arudenko.kanban.backend.model._

object TimeApiServiceImpl extends TimeApiService  with LazyLogging{
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Task not found
   */
  override def addTime(taskId: Int, time: Time)(implicit auth: Auth):Result[Time] = {
        Time.add(auth.user.id, taskId, record = time) match {
          case Some(value) => SuccessEntity(time.copy(id=Some(value.toInt)))
          case None => WrongInput("incorrect value")
        }
  }

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  override def deleteTimeRecord(recordId: Int)(implicit auth: Auth):Result[Time] =
            if(auth.user.admin)
              Time.delete(recordId) match {
                case 0 => NotFound
                case _ => SuccessEmpty
              }
            else
              Time.deleteForUser(auth.user.id,recordId) match {
                case 0 => NotFound
                case _ => SuccessEmpty
              }


  /**
   * Code: 200, Message: successful operation, DataType: Seq[Time]
   * Code: 404, Message: Task not found
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def getTime(taskId: Int)(implicit auth: Auth):Result[Seq[Time]] =
        SuccessEntity(Time.getByTask(taskId))
  /**
   * Code: 200, Message: successful operation, DataType: Time
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: Record not found
   */
  override def getTimeRecordById(recordId: Int)(implicit auth: Auth):Result[Time] =
        Time.get(recordId) match {
          case Some(value) => SuccessEntity(value)
          case None => NotFound
        }

  override def updateTime(taskId: Int, time: Time)(implicit auth: Auth):Result[Time] =
        Time.updateForUser(auth.user.id,taskId,time) match {
          case 0 => NotFound
          case _=> SuccessEmpty
        }

}
