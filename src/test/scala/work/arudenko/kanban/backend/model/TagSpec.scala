package work.arudenko.kanban.backend.model

import org.scalatest._
import flatspec._
import matchers._

class TagSpec extends AnyFlatSpec with should.Matchers {

  "Tag" should "return tags for issue id specified" in {
    val tags = Tag.getTagsForIssue(1)
    print(tags.head)
    tags.length should be > 0
  }

  it should "be able to find tag by name" in{
    val tags =Tag.getTagsByName(".*")
    print(tags.head)
    tags.length should be > 0
  }

}
