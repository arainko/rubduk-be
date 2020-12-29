package io.rubduk.infrastructure.tables

import io.rubduk.domain.models.media._
import io.rubduk.domain.models.user._
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.SlickPGProfile.api._

import java.time.OffsetDateTime

object Media {

  class Schema(tag: Tag) extends Table[MediumRecord](tag, "media") {
    def id        = column[MediumId]("id", O.PrimaryKey, O.AutoInc)
    def link      = column[Link]("link")
    def userId    = column[UserId]("user_id")
    def dateAdded = column[OffsetDateTime]("date_added")
    def *         = (id, userId, link, dateAdded).mapTo[MediumRecord]

    def user = foreignKey("user_fk", userId, Users.table)(user => user.id, onDelete = ForeignKeyAction.Restrict)
  }

  val table: TableQuery[Schema] = TableQuery[Schema]
}
