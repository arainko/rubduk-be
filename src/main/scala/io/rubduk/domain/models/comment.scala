package io.rubduk.domain.models

import cats.implicits.catsSyntaxOptionId
import io.rubduk.domain.models.post._
import io.rubduk.domain.models.user._
import io.scalaland.chimney.dsl._

import java.time.OffsetDateTime

object comment {
  final case class CommentId(value: Long) extends AnyVal

  final case class CommentRecord(
    id: Option[CommentId],
    contents: String,
    postId: PostId,
    userId: UserId,
    dateAdded: OffsetDateTime
  ) {

    def toDomain(user: User): Comment =
      this
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

    def toDAO(userId: UserId): CommentRecord =
      this
        .into[CommentRecord]
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

    def toDAO(postId: PostId, userId: UserId, dateAdded: OffsetDateTime): CommentRecord =
      this
        .into[CommentRecord]
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

  sealed trait CommentFilter
}
