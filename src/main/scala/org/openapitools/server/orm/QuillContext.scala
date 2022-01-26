package org.openapitools.server.orm

import scala.reflect.runtime.universe.Literal
import io.getquill.PostgresNdbcContext

object QuillContext extends PostgresNdbcContext(Literal, "ctx"){
}