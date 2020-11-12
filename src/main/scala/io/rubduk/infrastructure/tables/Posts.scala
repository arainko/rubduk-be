package io.rubduk.infrastructure.tables

import java.time.OffsetDateTime

import io.rubduk.infrastructure.converters.IdConverter._
import io.rubduk.infrastructure.models.{ PostDAO, PostId, UserId }
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._

object Posts {

  class Schema(tag: Tag) extends Table[PostDAO](tag, "posts") {
    def id        = column[PostId]("id", O.PrimaryKey, O.AutoInc)
    def contents  = column[String]("contents")
    def userId    = column[UserId]("user_id")
    def dateAdded = column[OffsetDateTime]("date_added")
    def *         = (id.?, contents, userId, dateAdded).mapTo[PostDAO]

    def user = foreignKey("user_fk", userId, Users.table)(user => user.id, onDelete = ForeignKeyAction.Restrict)
  }

  val table: TableQuery[Schema] = TableQuery[Posts.Schema]
}
