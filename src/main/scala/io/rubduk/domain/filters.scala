package io.rubduk.domain

import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.UserId
import io.rubduk.infrastructure.tables.{Comments, Posts}
import io.rubduk.infrastructure.typeclasses.IdConverter._

object filters {

  def postUserIdFilter(userId: Option[UserId]): Filter[Posts.Schema] =
    Filter.optional(userId)(_.userId === _)

  def commentUserIdFilter(userId: Option[UserId]): Filter[Comments.Schema] =
    Filter.optional(userId)(_.userId === _)
}
