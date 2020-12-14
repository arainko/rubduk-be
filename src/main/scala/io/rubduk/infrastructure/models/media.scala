package io.rubduk.infrastructure.models

import java.time.OffsetDateTime

import slick.lifted.MappedTo
import io.scalaland.chimney.dsl._

object media {
  final case class MediumId(value: Long)      extends AnyVal
  final case class Link(value: String)        extends AnyVal with MappedTo[String]
  final case class Base64Image(value: String) extends AnyVal

  final case class ImageData(link: Link, name: String)
  final case class ImgurImageResponse(data: ImageData, success: Boolean, status: Int)

  final case class MediumRecord(mediumId: Option[MediumId], userId: UserId, link: Link, dateAdded: OffsetDateTime) {

    def unsafeToOutRecord: MediumOutRecord =
      this
        .into[MediumOutRecord]
        .withFieldComputed(_.mediumId, _.mediumId.get)
        .transform
  }

  final case class MediumInRecord(userId: UserId, link: Link, dateAdded: OffsetDateTime) {

    def toRecord: MediumRecord =
      this
        .into[MediumRecord]
        .withFieldConst(_.mediumId, None)
        .transform
  }
  final case class MediumOutRecord(mediumId: MediumId, userId: UserId, link: Link, dateAdded: OffsetDateTime)
}
