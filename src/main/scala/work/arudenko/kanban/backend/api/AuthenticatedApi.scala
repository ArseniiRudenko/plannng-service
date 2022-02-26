package work.arudenko.kanban.backend.api

import akka.http.scaladsl.server.Route
import work.arudenko.kanban.backend.controller.Auth

trait AuthenticatedApi extends GenericApi {


  def route(implicit auth:Auth):Route

}
