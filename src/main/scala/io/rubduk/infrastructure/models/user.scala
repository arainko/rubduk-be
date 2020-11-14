package io.rubduk.infrastructure.models

import io.scalaland.chimney.dsl._

import java.time.{LocalDate, OffsetDateTime}

final case class UserId(value: Long) extends AnyVal

final case class UserDAO(
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
  def toDAO: UserDAO = this.transformInto[UserDAO]
  def toDTO: UserDTO = this.transformInto[UserDTO]
}

final case class UserDTO(
  id: Option[UserId],
  name: String,
  lastName: Option[String],
  email: String,
  dateOfBirth: Option[LocalDate],
  createdOn: Option[OffsetDateTime]
) {

  def toDAO(createdOn: OffsetDateTime): UserDAO =
    this
      .into[UserDAO]
      .withFieldConst(_.createdOn, createdOn)
      .transform

  def toDomain(createdOn: OffsetDateTime): User =
    this
      .into[User]
      .withFieldConst(_.createdOn, createdOn)
      .transform
}
