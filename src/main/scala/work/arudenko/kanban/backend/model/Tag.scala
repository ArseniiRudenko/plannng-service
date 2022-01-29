package work.arudenko.kanban.backend.model

/**
 * @param id  for example: ''null''
 * @param name  for example: ''null''
 * @param description  for example: ''null''
*/
final case class Tag (
  id: Option[Int],
  name: Option[String],
  description: Option[String]
)

