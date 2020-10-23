package io.rubduk.infrastructure.tables

import java.time.{LocalDate, OffsetDateTime}

import io.rubduk.infrastructure.converters.IdConverter._
import io.rubduk.infrastructure.models.{UserDAO, UserId}
import slick.jdbc.PostgresProfile.api._

object Users {
  class Schema(tag: Tag) extends Table[UserDAO](tag, "users") {
    def id    = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def name  = column[String]("name")
    def lastName = column[String]("last_name").?
    def dateOfBirth = column[LocalDate]("date_of_birth").?
    def createdOn = column[OffsetDateTime]("created_on")
    def *     = (id.?, name, lastName, dateOfBirth, createdOn).mapTo[UserDAO]
  }

  val table: TableQuery[Schema] = TableQuery[Users.Schema]
}
