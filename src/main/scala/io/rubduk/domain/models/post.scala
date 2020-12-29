package io.rubduk.domain.models

import cats.implicits.catsSyntaxOptionId
import io.rubduk.domain.models.user._
import io.scalaland.chimney.dsl._

import java.time.OffsetDateTime

object post {
  final case class PostId(value: Long) extends AnyVal

  final case class PostRecord(
    id: Option[PostId],
    contents: String,
    userId: UserId,
    dateAdded: OffsetDateTime
  ) {

    def toDomain(user: User): Post =
      this
        .into[Post]
        .withFieldConst(_.user, user)
        .transform
  }

  final case class Post(
    id: Option[PostId],
    contents: String,
    user: User,
    dateAdded: OffsetDateTime
  ) {

    def toDAO(userId: UserId): PostRecord =
      this
        .into[PostRecord]
        .withFieldRenamed(_.user, _.userId)
        .withFieldConst(_.userId, userId)
        .transform

    def toDTO: PostDTO =
      this
        .into[PostDTO]
        .withFieldComputed(_.dateAdded, _.dateAdded.some)
        .withFieldComputed(_.userId, _.user.id)
        .withFieldComputed(_.username, _.user.name.some)
        .withFieldComputed(_.userLastname, _.user.lastName)
        .transform
  }

  final case class PostDTO(
    id: Option[PostId],
    contents: String,
    username: Option[String],
    userLastname: Option[String],
    userId: Option[UserId],
    dateAdded: Option[OffsetDateTime]
  ) {

    def toDomain(user: User, dateAdded: OffsetDateTime): Post =
      this
        .into[Post]
        .withFieldConst(_.user, user)
        .withFieldConst(_.dateAdded, dateAdded)
        .transform
  }

  sealed trait PostFilter

  object PostFilter {
    final case class ByUser(userId: UserId) extends PostFilter
  }

}
