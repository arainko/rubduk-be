package io.rubduk

import java.time.OffsetDateTime
import java.util.UUID

import io.rubduk.domain.models.{Comment, Post, PostId, User, UserId}

object Entities {
  private def uuid: String = UUID.randomUUID.toString

  def user: User = User(None, s"USER-$uuid", None, s"${uuid.take(4)}@${uuid.take(4)}.com", None, OffsetDateTime.now)

  def post: Post = Post(None, uuid, user, OffsetDateTime.now)

  def post(user: User): Post = Post(None, uuid, user, OffsetDateTime.now)

  def comment(postId: PostId, userId: UserId): Comment = Comment(None, uuid, postId, userId, OffsetDateTime.now)
}
