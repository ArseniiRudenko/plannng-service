package work.arudenko.kanban.backend

import java.util.Base64

package object controller {

  lazy val encoder: Base64.Encoder = Base64.getEncoder
  lazy val decoder: Base64.Decoder = Base64.getDecoder

  implicit class Base64Encoder(val input:Array[Byte]) extends AnyVal{
    def toBase64: String = encoder.encodeToString(input)
  }

  implicit class Base64Decoder(val input:String) extends AnyVal{
    def asBase64:Array[Byte] = decoder.decode(input)
  }

}
