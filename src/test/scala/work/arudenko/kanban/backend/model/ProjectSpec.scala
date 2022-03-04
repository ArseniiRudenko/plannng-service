package work.arudenko.kanban.backend.model

import org.scalatest.flatspec._
import org.scalatest.matchers._

class ProjectSpec extends AnyFlatSpec with should.Matchers {

  "Project" should "retun list of projects for passed id-s" in{
    val proj = Project.getProjectList(Seq(1,6))
    proj.length shouldBe 2
  }

}
