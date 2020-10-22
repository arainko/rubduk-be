package io.rubduk.infrastructure.models

import java.time.OffsetDateTime

final case class CommentId(value: Long) extends AnyVal

final case class CommentDAO(
  id: Option[CommentId],
  contents: String,
  postId: PostId,
  userId: UserId,
  dateAdded: OffsetDateTime
)

case class Comment(
  id: Option[CommentId],
  contents: String,
  post: Post,
  user: User,
  dateAdded: OffsetDateTime
)

case class CommentDTO(
  id: Option[CommentId],
  contents: String,
  userName: String,
  dateAdded: OffsetDateTime
)
