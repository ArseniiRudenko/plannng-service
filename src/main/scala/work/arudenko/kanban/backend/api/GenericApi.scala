package work.arudenko.kanban.backend.api

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.{Authenticator, complete}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.Credentials
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model._
import work.arudenko.kanban.backend.orm.RedisContext.redis
import work.arudenko.kanban.backend.serialization.binary.UserApiMarshallerImpl.{userParser, userSerializer}

trait GenericApi {


  val authenticator:Authenticator[Auth] = {
    case Credentials.Missing => None
    case p:Credentials.Provided =>
      redis.withClient {
        client => client.get[User](p.identifier)(userSerializer,userParser).map(u=>{
          client.expire(p.identifier,Auth.authDuration.toSeconds.toInt)
          Auth(p.identifier,u)
        })
      }
  }

  protected implicit def resultRoute[T](value:Result[T])(implicit marshaller: ToEntityMarshaller[T]): Route = value match {
    case GeneralResult(code, message) => complete(code,message)
    case SuccessEmpty => complete((200,"Success"))
    case NotFound => complete((404,"Entity not found"))
    case NotAuthorized => complete((403,"User is not authorized to do that"))
    case WrongInput(message) => complete((400,message))
    case SuccessEntity(value) => complete((200, value))
  }
}
