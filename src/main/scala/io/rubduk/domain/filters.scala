package io.rubduk.domain

import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.models.UserId
import io.rubduk.infrastructure.tables.Posts
import io.rubduk.domain.typeclasses.IdConverter._
import io.rubduk.infrastructure.Filter

object filters {

  def postUserIdFilter(userId: Option[UserId]): Filter[Posts.Schema] =
    Filter.optional(userId)(_.userId === _)
}
