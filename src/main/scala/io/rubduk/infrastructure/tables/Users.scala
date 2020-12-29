package io.rubduk.infrastructure.tables

import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.models.user._
import io.rubduk.infrastructure.mappers._

import java.time.{LocalDate, OffsetDateTime}

object Users {

  class Schema(tag: Tag) extends Table[UserRecord](tag, "users") {
    def id          = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def name        = column[String]("name")
    def lastName    = column[String]("last_name").?
    def email       = column[String]("email")
    def dateOfBirth = column[LocalDate]("date_of_birth").?
    def createdOn   = column[OffsetDateTime]("created_on")
    def *           = (id.?, name, lastName, email, dateOfBirth, createdOn).mapTo[UserRecord]
  }

  val table: TableQuery[Schema] = TableQuery[Users.Schema]
}
