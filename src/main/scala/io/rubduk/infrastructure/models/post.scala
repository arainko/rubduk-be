package io.rubduk.infrastructure.models

import java.time.OffsetDateTime

final case class PostId(value: Long) extends AnyVal

final case class PostDAO(
  id: Option[PostId],
  contents: String,
  userId: UserId,
  dateAdded: OffsetDateTime
)

final case class Post(
  id: Option[PostId],
  contents: String,
  user: User,
  comments: Seq[Comment],
  dateAdded: OffsetDateTime
)

final case class PostDTO(
  id: Option[PostId],
  contents: String,
  user: UserDTO,
  comments: Seq[CommentDTO],
  dateAdded: OffsetDateTime
)
