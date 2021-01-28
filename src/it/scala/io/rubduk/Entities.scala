package io.rubduk

import java.time.OffsetDateTime
import java.util.UUID

import io.rubduk.domain.models.user._
import io.rubduk.domain.models.post._
import io.rubduk.domain.models.comment._

object Entities {
  private def uuid: String = UUID.randomUUID.toString

  def user: User =
    User(
      None,
      s"USER-$uuid",
      None,
      s"${uuid.take(4)}@${uuid.take(4)}.com",
      None,
      None,
      OffsetDateTime.now
    )

  def post: Post = Post(None, uuid, 0, user, OffsetDateTime.now)

  def post(user: User): Post = Post(None, uuid, 0, user, OffsetDateTime.now)

  def comment(postId: PostId, user: User): Comment = Comment(None, uuid, postId, user, OffsetDateTime.now)
}
