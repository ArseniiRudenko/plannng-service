package work.arudenko.kanban.backend

import java.util.Base64

package object controller {

  implicit class Base64Encoder(val input:Array[Byte]) extends AnyVal{
      def toBase64: String = Base64.getEncoder.encodeToString(input)
  }

  implicit class Base64Decoder(val input:String) extends AnyVal{
    def asBase64:Array[Byte] = Base64.getDecoder.decode(input)
  }


}
