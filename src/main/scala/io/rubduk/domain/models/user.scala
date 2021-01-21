package io.rubduk.domain.models

import io.rubduk.domain.models.media.{Link, Medium, MediumId}
import io.scalaland.chimney.dsl._

import java.time.{LocalDate, OffsetDateTime}

object user {
  final case class UserId(value: Long) extends AnyVal

  final case class UserRecord(
    id: Option[UserId],
    name: String,
    lastName: Option[String],
    email: String,
    profilePicId: Option[MediumId],
    dateOfBirth: Option[LocalDate],
    createdOn: OffsetDateTime
  ) {

    def toDomain(profilePic: Option[Medium]): User =
      this
        .into[User]
        .withFieldConst(_.profilePic, profilePic)
        .transform
  }

  final case class User(
    id: Option[UserId],
    name: String,
    lastName: Option[String],
    email: String,
    profilePic: Option[Medium],
    dateOfBirth: Option[LocalDate],
    createdOn: OffsetDateTime
  ) {

    def toDAO: UserRecord =
      this
        .into[UserRecord]
        .withFieldComputed(_.profilePicId, _.profilePic.map(_.mediumId))
        .transform

    def toDTO: UserDTO =
      this
        .into[UserDTO]
        .withFieldComputed(_.profilePic, _.profilePic.map(_.link))
        .transform
  }

  final case class UserDTO(
    id: Option[UserId],
    name: String,
    lastName: Option[String],
    email: String,
    profilePic: Option[Link],
    dateOfBirth: Option[LocalDate],
    createdOn: Option[OffsetDateTime]
  ) {

    def toDAO(createdOn: OffsetDateTime): UserRecord =
      this
        .into[UserRecord]
        .withFieldConst(_.createdOn, createdOn)
        .withFieldConst(_.profilePicId, None)
        .transform

    def toDomain(createdOn: OffsetDateTime): User =
      this
        .into[User]
        .withFieldConst(_.createdOn, createdOn)
        .withFieldConst(_.profilePic, None)
        .transform
  }

  sealed trait UserFilter

  object UserFilter {
    final case class ByEmail(email: String)       extends UserFilter
    final case class ById(id: UserId)             extends UserFilter
    final case class NameContaining(name: String) extends UserFilter
  }

}
