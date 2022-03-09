package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, PathMatcher0, Route}
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.util.Tuple
import com.typesafe.scalalogging.LazyLogging
import work.arudenko.kanban.backend.controller.Auth
import work.arudenko.kanban.backend.model.User
import work.arudenko.kanban.backend.orm.RedisContext.redis
import work.arudenko.kanban.backend.serialization.binary.UserApiMarshallerImpl.{userParser, userSerializer}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class AuthenticatedApi(prefix:String)(implicit ex:ExecutionContext) extends GenericApi with LazyLogging {

  protected val authenticator:Authenticator[Auth] = {
    case Credentials.Missing => None
    case p:Credentials.Provided =>
      redis.withClient {
        client => client.get[User](p.identifier)(userSerializer,userParser).map(u=>{
          Future {
            redis.withClient {
              client =>
                client.expire(p.identifier, Auth.authDuration.toSeconds.toInt)
            }
          }.onComplete({
            case Failure(exception) => logger.error(s"failed to extend expiration for $u ",exception)
            case Success(value) => logger.debug(s"successful expiration extension for $u status $value")
          })
          Auth(p.identifier,u)
        })
      }
  }

  protected def route(implicit auth:Auth):Route

  val authenticatedRoute:Route = pathPrefix(prefix){
    authenticateOAuth2("Global", authenticator) {
      implicit auth => route
    }
  }

}
