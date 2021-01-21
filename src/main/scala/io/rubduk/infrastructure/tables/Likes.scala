package io.rubduk.infrastructure.tables

import io.rubduk.domain.models.post._
import io.rubduk.domain.models.user._
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.mappers._

object Likes {

  class Schema(tag: Tag) extends Table[Like](tag, "likes") {
    def postId    = column[PostId]("postid")
    def userId    = column[UserId]("userid")
    def *         = (postId, userId).mapTo[Like]
  }

  val table: TableQuery[Schema] = TableQuery[Likes.Schema]
}
