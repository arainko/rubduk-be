package io.rubduk.infrastructure.models

import io.scalaland.chimney.dsl._

import java.time.OffsetDateTime

final case class PostId(value: Long) extends AnyVal

final case class PostDAO(
  id: Option[PostId],
  contents: String,
  userId: UserId,
  dateAdded: OffsetDateTime
) {
  def toDomain(comments: Seq[Comment], user: User): Post =
    this.into[Post]
    .withFieldConst(_.comments, comments)
    .withFieldRenamed(_.userId, _.user)
    .withFieldConst(_.user, user)
    .transform

  def toDTO(user: UserDTO, comments: Seq[CommentDTO]): PostDTO =
    this.into[PostDTO]
      .withFieldConst(_.comments, comments)
      .withFieldRenamed(_.userId, _.user)
      .withFieldConst(_.user, user)
      .transform
}

final case class Post(
  id: Option[PostId],
  contents: String,
  user: User,
  comments: Seq[Comment],
  dateAdded: OffsetDateTime
) {
  def toDAO(userId: UserId): PostDAO =
    this.into[PostDAO]
      .withFieldRenamed(_.user, _.userId)
      .withFieldConst(_.userId, userId)
      .transform

  def toDTO(user: UserDTO, comments: Seq[CommentDTO]): PostDTO =
    this.into[PostDTO]
      .withFieldConst(_.user, user)
      .withFieldConst(_.comments, comments)
      .transform
}

final case class PostDTO(
  id: Option[PostId],
  contents: String,
  user: UserDTO,
  comments: Seq[CommentDTO],
  dateAdded: OffsetDateTime
) {
  def toDAO(userId: UserId): PostDAO =
    this.into[PostDAO]
      .withFieldRenamed(_.user, _.userId)
      .withFieldConst(_.userId, userId)
      .transform

  def toDomain(user: User, comments: Seq[Comment]): Post =
    this.into[Post]
      .withFieldConst(_.user, user)
      .withFieldConst(_.comments, comments)
      .transform
}