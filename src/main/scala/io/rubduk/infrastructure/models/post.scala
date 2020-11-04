package io.rubduk.infrastructure.models

import io.scalaland.chimney.dsl._
import java.time.OffsetDateTime

import cats.implicits.catsSyntaxOptionId

final case class PostId(value: Long) extends AnyVal

final case class PostDAO(
  id: Option[PostId],
  contents: String,
  userId: UserId,
  dateAdded: OffsetDateTime
) {
  def toDomain(user: User): Post =
    this.into[Post]
    .withFieldConst(_.user, user)
    .transform
}

final case class Post(
  id: Option[PostId],
  contents: String,
  user: User,
  dateAdded: OffsetDateTime
) {
  def toDAO(userId: UserId): PostDAO =
    this.into[PostDAO]
      .withFieldRenamed(_.user, _.userId)
      .withFieldConst(_.userId, userId)
      .transform

  def toDTO: PostDTO =
    this.into[PostDTO]
      .withFieldComputed(_.user, _.user.toDTO)
      .transform
}

final case class PostDTO(
  id: Option[PostId],
  contents: String,
  user: UserDTO,
  dateAdded: OffsetDateTime
) {
  def toDomain: Post =
    this.into[Post]
      .withFieldComputed(_.user, _.user.toDomain)
      .transform
}