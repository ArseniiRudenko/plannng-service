package work.arudenko.kanban.backend

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.stream.Materializer
import work.arudenko.kanban.backend.api.{CommentApi, TaskApi, TimeApi, UserApi}

class Controller(comment: CommentApi, task: TaskApi, time: TimeApi, user: UserApi)(implicit system: ActorSystem, materializer: Materializer) {

    lazy val routes: Route = comment.route ~ task.route ~ time.route ~ user.route 

    Http().newServerAt("0.0.0.0", 9000).bindFlow(routes)
}