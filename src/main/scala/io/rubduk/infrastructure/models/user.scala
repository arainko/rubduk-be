package io.rubduk.infrastructure.models

import java.time.{LocalDate, OffsetDateTime}

final case class UserId(value: Long) extends AnyVal

final case class UserDAO(
  id: Option[UserId],
  name: String,
  lastName: Option[String],
  dateOfBirth: Option[LocalDate],
  createdOn: OffsetDateTime
)

final case class User(
  id: Option[UserId],
  name: String,
  lastName: Option[String],
  dateOfBirth: Option[LocalDate],
  createdOn: OffsetDateTime
)

final case class UserDTO(
  id: Option[UserId],
  name: String,
  lastName: Option[String],
  dateOfBirth: Option[LocalDate],
  createdOn: OffsetDateTime
)
