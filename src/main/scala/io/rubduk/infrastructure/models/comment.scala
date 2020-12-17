package io.rubduk.infrastructure.models

import java.time.OffsetDateTime

import cats.implicits.catsSyntaxOptionId
import io.scalaland.chimney.dsl._

final case class CommentId(value: Long) extends AnyVal

final case class CommentDAO(
  id: Option[CommentId],
  contents: String,
  postId: PostId,
  userId: UserId,
  dateAdded: OffsetDateTime
) {
  def toDomain(user: User): Comment = this
    .into[Comment]
    .withFieldConst(_.user, user)
    .transform

}

case class Comment(
  id: Option[CommentId],
  contents: String,
  postId: PostId,
  user: User,
  dateAdded: OffsetDateTime
) {
  def toDAO(userId: UserId): CommentDAO =
    this.into[CommentDAO]
      .withFieldConst(_.userId, userId)
      .transform

  def toDTO: CommentDTO =
    this
      .into[CommentDTO]
      .withFieldComputed(_.name, _.user.name.some)
      .withFieldComputed(_.lastName, _.user.lastName)
      .withFieldComputed(_.userId, _.user.id)
      .transform
}

case class CommentDTO(
  id: Option[CommentId],
  contents: String,
  name: Option[String],
  lastName: Option[String],
  userId: Option[UserId],
  dateAdded: Option[OffsetDateTime]
) {

  def toDAO(postId: PostId, userId: UserId, dateAdded: OffsetDateTime): CommentDAO =
    this.into[CommentDAO]
      .withFieldConst(_.postId, postId)
      .withFieldConst(_.userId, userId)
      .withFieldConst(_.dateAdded, dateAdded)
      .transform

  def toDomain(postId: PostId, user: User, dateAdded: OffsetDateTime): Comment =
    this
      .into[Comment]
      .withFieldConst(_.postId, postId)
      .withFieldConst(_.user, user)
      .withFieldConst(_.dateAdded, dateAdded)
      .transform
}
