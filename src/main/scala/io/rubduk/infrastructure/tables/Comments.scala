package io.rubduk.infrastructure.tables

import java.time.OffsetDateTime

import io.rubduk.infrastructure.converters.IdConverter._
import io.rubduk.infrastructure.models.{CommentDAO, CommentId, PostId, UserId}
import slick.jdbc.PostgresProfile.api._

object Comments {

  class Schema(tag: Tag) extends Table[CommentDAO](tag, "comments") {
    def id        = column[CommentId]("id", O.PrimaryKey, O.AutoInc)
    def contents  = column[String]("contents")
    def postId    = column[PostId]("post_id")
    def userId    = column[UserId]("user_id")
    def dateAdded = column[OffsetDateTime]("date_added")
    def *         = (id.?, contents, postId, userId, dateAdded).mapTo[CommentDAO]

    def user = foreignKey("user_fk", userId, Users.table)(user => user.id, onDelete = ForeignKeyAction.Restrict)
    def post = foreignKey("post_fk", postId, Posts.table)(post => post.id, onDelete = ForeignKeyAction.Restrict)
  }

  val table: TableQuery[Schema] = TableQuery[Comments.Schema]
}
