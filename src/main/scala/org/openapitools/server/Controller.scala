package org.openapitools.server

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import org.openapitools.server.api.CommentApi
import org.openapitools.server.api.TaskApi
import org.openapitools.server.api.TimeApi
import org.openapitools.server.api.UserApi
import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.stream.Materializer

class Controller(comment: CommentApi, task: TaskApi, time: TimeApi, user: UserApi)(implicit system: ActorSystem, materializer: Materializer) {

    lazy val routes: Route = comment.route ~ task.route ~ time.route ~ user.route 

    Http().newServerAt("0.0.0.0", 9000).bindFlow(routes)
}