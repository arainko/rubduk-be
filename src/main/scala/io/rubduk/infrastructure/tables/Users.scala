package io.rubduk.infrastructure.tables

import java.time.{LocalDate, OffsetDateTime}

import io.rubduk.infrastructure.typeclasses.IdConverter._
import io.rubduk.infrastructure.models.{UserDAO, UserId}
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._

object Users {

  class Schema(tag: Tag) extends Table[UserDAO](tag, "users") {
    def id          = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def name        = column[String]("name")
    def lastName    = column[String]("last_name").?
    def email       = column[String]("email")
    def dateOfBirth = column[LocalDate]("date_of_birth").?
    def createdOn   = column[OffsetDateTime]("created_on")
    def *           = (id.?, name, lastName, email, dateOfBirth, createdOn).mapTo[UserDAO]
  }

  val table: TableQuery[Schema] = TableQuery[Users.Schema]
}
