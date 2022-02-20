package work.arudenko.kanban.backend.controller

import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.{MatchPatternHelper, should}
import work.arudenko.kanban.backend.model.SuccessEntity

class UserApiImplSpec extends AnyFlatSpec with should.Matchers {


  it should "login user with login and password" in {
    implicit val actorSystem: ActorSystem =ActorSystem("test")
    val impl = new UserApiServiceImpl
    val res =impl.loginUser("arsenii@test","test")
    println(res)
    res should matchPattern{case SuccessEntity(e)=> }
  }

}
