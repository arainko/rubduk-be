package io.rubduk.infrastructure.models

import java.time.{Instant, LocalDateTime}

object media {
  final case class MediumId(value: Long)      extends AnyVal
  final case class Link(value: String)        extends AnyVal
  final case class DeleteHash(value: String)  extends AnyVal
  final case class Base64Image(value: String) extends AnyVal

  sealed abstract class Medium

  final case class Image(
    link: Link,
    deletehash: DeleteHash,
    datetime: Long
  ) extends Medium

  final case class Video(
    id: Option[MediumId],
    link: Link,
    deleteHash: DeleteHash,
    createdAt: LocalDateTime
  ) extends Medium
}
