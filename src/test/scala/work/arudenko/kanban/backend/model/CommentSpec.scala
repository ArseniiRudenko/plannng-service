package work.arudenko.kanban.backend.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest._
import flatspec._
import matchers._


class CommentSpec extends AnyFlatSpec with should.Matchers {


  "Comment" should "get list of comments by issue id" in {
    val cmntsLst= Comment.getByIssueId(1)
    cmntsLst.length should be >1
  }

}
