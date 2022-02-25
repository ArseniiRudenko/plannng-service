package work.arudenko.kanban.backend.model

import org.scalatest.{flatspec,matchers}
import org.scalatest.{Tag=>ScalatestTag}
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
    tsk.length should be > 0
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

  it should "add new task and tag mappings from object" in{
    val header = "some new fancy task generated by test"
    val newTsk = Task(header = header,tags=Set(Tag.get(1).get,Tag.get(2).get),project = 1)
    val id=Task.addNew(newTsk,1)
    println(id)
    Task.delete(id.get.toInt)
    Task.get(id.get.toInt) shouldBe None
    Task.getByHeader(header) shouldBe Nil
  }

  it should "fail gracefully if tags are incorrect" in{
    val header = "task generated by test for tag failure 1"
    val newTsk = Task(header = header,tags=Set(Tag.get(1).get,Tag(Some(3),"non existent tag",None)),project=1)
    val id=Task.addNew(newTsk,1)

    val header2 = "task generated by test for tag failure 2"
    val newTsk2 = Task(header =header2,tags=Set(Tag.get(1).get,Tag(None,"broken tag with no id",None)),project=1)
    val id2=Task.addNew(newTsk2,1)
    id shouldBe None
    id2 shouldBe None
    Task.getByHeader(header) shouldBe Nil
    Task.getByHeader(header2) shouldBe Nil
  }

}