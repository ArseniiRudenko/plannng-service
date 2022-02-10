package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.{authenticateOAuth2, complete}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.model.{GeneralError, WithId}

trait GenericApi[T<:WithId[T]] extends AuthenticatedRoute with LazyLogging {


  def result400(responseGeneralError: GeneralError)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    complete((400, responseGeneralError))

  def result200(resValue: T)(implicit toEntityMarshaller: ToEntityMarshaller[T]): Route =
    complete((200, resValue))


  def addAPI(value: T,generator:(T,Int)=>Option[Long])(implicit toEntityMarshallerTask: ToEntityMarshaller[T], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    authenticateOAuth2("Global",authenticator) {
      auth =>
        generator(value, auth.user.id) match {
          case Some(genId) => result200(value.updateId(Some(genId.toInt)))
          case None => result400(GeneralError("incorrect task parameters"))
        }
    }

}
