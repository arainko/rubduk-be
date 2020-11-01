package io.rubduk.infrastructure.models

import java.time.OffsetDateTime

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
  def toDTO: CommentDTO = this.transformInto[CommentDTO]
}

case class CommentDTO(
  id: Option[CommentId],
  contents: String
) {
  def toDomain(postId: PostId, userId: UserId, dateAdded: OffsetDateTime): Comment =
    this.into[Comment]
      .withFieldConst(_.postId, postId)
      .withFieldConst(_.userId, userId)
      .withFieldConst(_.dateAdded, dateAdded)
      .transform
}
