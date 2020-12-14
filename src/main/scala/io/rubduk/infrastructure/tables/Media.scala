package io.rubduk.infrastructure.tables

import java.time.OffsetDateTime

import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.UserId
import io.rubduk.infrastructure.models.media._
import io.rubduk.infrastructure.typeclasses.IdConverter._

object Media {

  class Schema(tag: Tag) extends Table[MediumRecord](tag, "media") {
    def id        = column[MediumId]("id", O.PrimaryKey, O.AutoInc)
    def link      = column[Link]("link")
    def userId    = column[UserId]("user_id")
    def dateAdded = column[OffsetDateTime]("date_added")
    def *         = (id.?, userId, link, dateAdded).mapTo[MediumRecord]

    def user = foreignKey("user_fk", userId, Users.table)(user => user.id, onDelete = ForeignKeyAction.Restrict)
  }

  val table: TableQuery[Schema] = TableQuery[Schema]
}
