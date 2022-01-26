package org.openapitools.server.model

import java.time.OffsetDateTime

/**
 * @param id  for example: ''null''
 * @param text  for example: ''null''
 * @param author  for example: ''null''
 * @param createdAt  for example: ''null''
*/
final case class Comment (
  id: Option[Int],
  text: String,
  author: Option[User],
  createdAt: Option[OffsetDateTime]
)

