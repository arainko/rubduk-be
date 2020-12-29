package io.rubduk.domain.models

import io.scalaland.chimney.dsl._

import java.time.{LocalDate, OffsetDateTime}

object user {
  final case class UserId(value: Long) extends AnyVal

  final case class UserRecord(
    id: Option[UserId],
    name: String,
    lastName: Option[String],
    email: String,
    dateOfBirth: Option[LocalDate],
    createdOn: OffsetDateTime
  ) {
    def toDomain: User = this.transformInto[User]
    def toDTO: UserDTO = this.transformInto[UserDTO]
  }

  final case class User(
    id: Option[UserId],
    name: String,
    lastName: Option[String],
    email: String,
    dateOfBirth: Option[LocalDate],
    createdOn: OffsetDateTime
  ) {
    def toDAO: UserRecord = this.transformInto[UserRecord]
    def toDTO: UserDTO    = this.transformInto[UserDTO]
  }

  final case class UserDTO(
    id: Option[UserId],
    name: String,
    lastName: Option[String],
    email: String,
    dateOfBirth: Option[LocalDate],
    createdOn: Option[OffsetDateTime]
  ) {

    def toDAO(createdOn: OffsetDateTime): UserRecord =
      this
        .into[UserRecord]
        .withFieldConst(_.createdOn, createdOn)
        .transform

    def toDomain(createdOn: OffsetDateTime): User =
      this
        .into[User]
        .withFieldConst(_.createdOn, createdOn)
        .transform
  }

  sealed trait UserFilter

}
