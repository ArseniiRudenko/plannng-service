package work.arudenko.kanban.backend.model

import org.scalatest._
import flatspec._
import matchers._

class TaskSpec extends AnyFlatSpec with should.Matchers {

 "Task" should "return record when querying by id" in{
    val tsk = Task.get(1)
   print(tsk)
   tsk.isDefined shouldBe true
 }

  it should "return record when querying by status" in{
    val tsk = Task.getByStatus("closed")
    print(tsk.length)
    tsk.length should be > 1
  }

  it should "return records by regex header match" in{
    val tsk = Task.getByHeader("some.*")
    print(tsk.length)
    tsk.length should be > 1
  }

  it should "look up issues by tag id" in{
    val tsk = Task.getByTagId(2)
    print(tsk.length)
    tsk.length should be > 0
  }

}