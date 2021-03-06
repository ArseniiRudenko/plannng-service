package work.arudenko.kanban.backend.api

import akka.http.javadsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{Authenticator, complete}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.Credentials
import akka.util.ByteString
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model._
import work.arudenko.kanban.backend.orm.RedisContext.redis
import work.arudenko.kanban.backend.serialization.binary.UserApiMarshallerImpl.{userParser, userSerializer}

trait GenericApi {



  implicit val unitMarshaller:ToEntityMarshaller[Unit] = Marshaller.opaque((_:Unit) => HttpEntity.Empty)

  protected implicit def resultRoute[T](value:Result[T])(implicit marshaller: ToEntityMarshaller[T]): Route = value match {
    case GeneralResult(code, message) => complete(code,message)
    case SuccessEmpty => complete((200,"Success"))
    case NotFound => complete((404,"Entity not found"))
    case NotAuthorized => complete((403,"User is not authorized to do that"))
    case WrongInput(message) => complete((400,message))
    case SuccessEntity(value) => complete((200, value))

  }
}
