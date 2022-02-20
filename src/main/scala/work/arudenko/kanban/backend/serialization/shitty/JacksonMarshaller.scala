package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.{HttpEntity, RequestEntity}
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag

trait JacksonMarshaller {

  val mapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  protected def getMarshaller[T](implicit classTag: ClassTag[T]): Marshaller[T, RequestEntity] =
    Marshaller.combined[T, String, RequestEntity](c => mapper.writerFor(classTag.runtimeClass).writeValueAsString(c))

  protected def getUnmarshaller[T](implicit classTag: ClassTag[T]): Unmarshaller[HttpEntity, T] =
    stringUnmarshaller.map(str => mapper.readerFor(classTag.runtimeClass).readValue[T](str))

}
