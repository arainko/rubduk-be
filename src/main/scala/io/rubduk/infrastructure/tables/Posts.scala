package io.rubduk.infrastructure.tables

import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.models.post._
import io.rubduk.domain.models.user._
import io.rubduk.infrastructure.mappers._

import java.time.OffsetDateTime

object Posts {

  class Schema(tag: Tag) extends Table[PostRecord](tag, "posts") {
    def id        = column[PostId]("id", O.PrimaryKey, O.AutoInc)
    def contents  = column[String]("contents")
    def userId    = column[UserId]("user_id")
    def dateAdded = column[OffsetDateTime]("date_added")
    def *         = (id.?, contents, userId, dateAdded).mapTo[PostRecord]

    def user = foreignKey("user_fk", userId, Users.table)(user => user.id, onDelete = ForeignKeyAction.Restrict)
  }

  val table: TableQuery[Schema] = TableQuery[Posts.Schema]
}
