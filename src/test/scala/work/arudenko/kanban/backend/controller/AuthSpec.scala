package work.arudenko.kanban.backend.controller

import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class AuthSpec extends AnyFlatSpec with should.Matchers {

  "Auth" should "return encoded password for plaintext one, and match it with plaintext" in{
    val plaintext="test"
    val encoded=Auth.hashPassword(plaintext)
    println(encoded)
    Auth.verifyPassword(plaintext,encoded) shouldBe true
  }

  it should "still work for non ascii passwords" in{
    val plaintext="Вот это тоже валидный пароль"
    val encoded=Auth.hashPassword(plaintext)
    Auth.verifyPassword(plaintext,encoded) shouldBe true
  }


}
