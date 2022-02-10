package work.arudenko.kanban.backend.model

trait WithId[T] {

  def getId:Option[Int]
  def updateId(newId:Option[Int]):T

}
