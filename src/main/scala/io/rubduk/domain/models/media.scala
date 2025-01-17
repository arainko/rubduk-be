package io.rubduk.domain.models

import io.rubduk.domain.models.user._

import java.time.OffsetDateTime
import slick.lifted.MappedTo
import io.scalaland.chimney.dsl._

object media {
  final case class MediumId(value: Long)      extends AnyVal
  final case class Link(value: String)        extends AnyVal with MappedTo[String]
  final case class Base64Image(value: String) extends AnyVal
  final case class ImageRequest(base64Image: Base64Image, description: Option[String])

  final case class ImageData(link: Link, name: String)
  final case class ImgurImageResponse(data: ImageData, success: Boolean, status: Int)

  final case class MediumInRecord(
    userId: UserId,
    link: Link,
    description: Option[String],
    dateAdded: OffsetDateTime
  ) {

    def toOutRecord(mediumId: MediumId): MediumRecord =
      this
        .into[MediumRecord]
        .withFieldConst(_.mediumId, mediumId)
        .transform
  }

  final case class MediumRecord(
    mediumId: MediumId,
    userId: UserId,
    link: Link,
    description: Option[String],
    dateAdded: OffsetDateTime
  ) {
    def toDomain: Medium = this.transformInto[Medium]
  }

  final case class Medium(
    mediumId: MediumId,
    userId: UserId,
    link: Link,
    description: Option[String],
    dateAdded: OffsetDateTime
  ) {
    def toDTO: MediumDTO = this.transformInto[MediumDTO]
  }

  final case class MediumDTO(
    mediumId: MediumId,
    userId: UserId,
    link: Link,
    description: Option[String],
    dateAdded: OffsetDateTime
  )

  sealed trait MediaFilter

  object MediaFilter {
    final case class ByUser(userId: UserId) extends MediaFilter
  }
}
