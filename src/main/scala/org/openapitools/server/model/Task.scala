package org.openapitools.server.model

import java.time.OffsetDateTime

/**
 * @param id  for example: ''null''
 * @param header  for example: ''do this''
 * @param description  for example: ''this is what should be the result''
 * @param parentId  for example: ''null''
 * @param deadline  for example: ''null''
 * @param assigneeId  for example: ''null''
 * @param estimatedTime  for example: ''null''
 * @param photoUrls  for example: ''null''
 * @param tags  for example: ''null''
 * @param status task status for example: ''null''
*/
final case class Task (
  id: Option[Int],
  header: String,
  description: Option[String],
  parentId: Option[Int],
  deadline: Option[OffsetDateTime],
  assigneeId: Option[Int],
  estimatedTime: Option[Int],
  photoUrls: Option[Seq[String]],
  tags: Option[Seq[Tag]],
  status: Option[String]
)

