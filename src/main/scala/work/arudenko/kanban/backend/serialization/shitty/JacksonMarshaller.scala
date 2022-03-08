package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.{HttpEntity, RequestEntity}
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait JacksonMarshaller {

  val mapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  protected def getMarshaller[T](implicit classTag: ClassTag[T]): Marshaller[T, RequestEntity] = {
    val writer = mapper.writerFor(classTag.runtimeClass)
    Marshaller.combined[T, String, RequestEntity](c => writer.writeValueAsString(c))
  }

  protected def getUnmarshaller[T](implicit classTag: ClassTag[T]): Unmarshaller[HttpEntity, T] = {
    val reader = mapper.readerFor(classTag.runtimeClass)
    stringUnmarshaller.map(str => reader.readValue[T](str))
  }

}
