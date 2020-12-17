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
  def toDomain: Comment = this.transformInto[Comment]
  def toDTO: CommentDTO = this.transformInto[CommentDTO]
}

case class Comment(
  id: Option[CommentId],
  contents: String,
  postId: PostId,
  userId: UserId,
  dateAdded: OffsetDateTime
) {
  def toDAO: CommentDAO = this.transformInto[CommentDAO]
  def toDTO: CommentDTO = this.into[CommentDTO]
    .withFieldComputed(_.userId, _.userId.some)
    .transform
}

case class CommentDTO(
  id: Option[CommentId],
  userId: Option[UserId],
  contents: String
) {

  def toDomain(postId: PostId, userId: UserId, dateAdded: OffsetDateTime): Comment =
    this
      .into[Comment]
      .withFieldConst(_.postId, postId)
      .withFieldConst(_.userId, userId)
      .withFieldConst(_.dateAdded, dateAdded)
      .transform
}
