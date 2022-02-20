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
    println(encoded)
    Auth.verifyPassword(plaintext,encoded) shouldBe true
  }

  it should "verify plaintext password against generated" in{
    val gen="vUQF+TCLYHwsqNDIXB1fxZ6GrhIKRj96Qdo+8Otl05/rtilWGq8TJPhPEq7/Ta66JT6NFnPaNRdIYbJLJddEAsQqHkaN62ivEePdBeyXReqpoLOE+6ap89HHDIFcsTG7jhSl1P97vgM/AfGtcTHSHXqL7TRBRwEoiuTg10rNc8A=:lspsctnAakLDdHcgTn8UrlfCkPP+IUJs4xF4FM86iKnAG4DYqmkMtqXKnXnqkJJFZWVRjfyOze0nPgUE0zVqtp4+yTNyRTY5r3hMYJA5faFLU4M5yUqD8y48aNSbu8YPqbDBKKo9M4Z9Oof5vmYoeOGe0FZJj4omskmwy3HVAG4="
    val plain = "test"
    Auth.verifyPassword(plain,gen) shouldBe true
  }

}
