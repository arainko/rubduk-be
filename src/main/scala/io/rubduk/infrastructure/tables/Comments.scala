package io.rubduk.infrastructure.tables

import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.models.comment._
import io.rubduk.domain.models.post._
import io.rubduk.domain.models.user._
import io.rubduk.infrastructure.mappers._

import java.time.OffsetDateTime

object Comments {

  class Schema(tag: Tag) extends Table[CommentRecord](tag, "comments") {
    def id        = column[CommentId]("id", O.PrimaryKey, O.AutoInc)
    def contents  = column[String]("contents")
    def postId    = column[PostId]("post_id")
    def userId    = column[UserId]("user_id")
    def dateAdded = column[OffsetDateTime]("date_added")
    def *         = (id.?, contents, postId, userId, dateAdded).mapTo[CommentRecord]

    def user = foreignKey("user_fk", userId, Users.table)(user => user.id, onDelete = ForeignKeyAction.Restrict)
    def post = foreignKey("post_fk", postId, Posts.table)(post => post.id, onDelete = ForeignKeyAction.Restrict)
  }

  val table: TableQuery[Schema] = TableQuery[Comments.Schema]
}
