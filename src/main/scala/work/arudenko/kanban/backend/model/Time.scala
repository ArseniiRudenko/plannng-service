package work.arudenko.kanban.backend.model

import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * @param id  for example: ''null''
 * @param description  for example: ''null''
 * @param date  for example: ''null''
 * @param time  for example: ''null''
 * @param createdAt  for example: ''null''
*/
final case class Time (
  id: Option[Int],
  description: String,
  date: LocalDate,
  time: Int,
  createdAt: Option[OffsetDateTime]
)

