package work.arudenko.kanban.backend

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.stream.Materializer
import work.arudenko.kanban.backend.api._

class Controller(comment: CommentApi, task: TaskApi, time: TimeApi, user: UserApi,adminUserApi: AdminUserApi)(implicit system: ActorSystem, materializer: Materializer) {

    lazy val routes: Route = comment.route ~ task.route ~ time.route ~ user.route ~ adminUserApi.route

    Http().newServerAt("0.0.0.0", 9000).bindFlow(routes)
}