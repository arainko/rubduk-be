package io.rubduk.domain

import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.UserId
import io.rubduk.infrastructure.tables.Posts
import io.rubduk.infrastructure.typeclasses.IdConverter._

object filters {

  def postUserIdFilter(userId: Option[UserId]): Filter[Posts.Schema] =
    Filter.optional(userId)(_.userId === _)
}
