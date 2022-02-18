package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.{HttpEntity, RequestEntity}
import akka.http.scaladsl.unmarshalling.{PredefinedFromEntityUnmarshallers, Unmarshaller}
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.util.ByteString
import boopickle.Default.{Pickle, Pickler, Unpickle}
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

trait JacksonMarshaller {

  val mapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  protected def getMarshaller[T]: Marshaller[T, RequestEntity] =
    Marshaller.combined[T, String, RequestEntity](c => mapper.writer().writeValueAsString(c))

  protected def getUnmarshaller[T]: Unmarshaller[HttpEntity, T] =
    stringUnmarshaller.map(str => mapper.reader().readValue[T](str))

}
