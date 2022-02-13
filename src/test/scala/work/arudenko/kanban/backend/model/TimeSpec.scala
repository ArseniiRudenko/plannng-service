package work.arudenko.kanban.backend.model

import org.scalatest._
import flatspec._
import matchers._

import java.time.{Duration, Instant, LocalDate, LocalTime}

class TimeSpec extends AnyFlatSpec with should.Matchers {

  "Time" should "be able to create a record and return valid id" in {
    val newTime = Time(None,"time spec test",LocalDate.now(),LocalTime.of(1,10,1),None)
    val id = Time.add(1,1,newTime)
    val recordedTime = Time.get(id.get.toInt)
    print(recordedTime.get)
  }

  it should "be able to update the record correctly" in {
    val record = Time.get(1).get
    val updatedRecord = record.copy(time=record.time.plus(Duration.ofHours(1)))
    val numUpdated =Time.updateForUser(1,1,updatedRecord)
    numUpdated shouldBe 1
    val recordCheck = Time.get(1).get
    recordCheck.time shouldBe updatedRecord.time
  }

}
