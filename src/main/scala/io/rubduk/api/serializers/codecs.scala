package io.rubduk.api.serializers

import java.time.{LocalDateTime, ZoneOffset}

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Codec, Decoder, Encoder}
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.models.media._
import io.rubduk.infrastructure.typeclasses.IdConverter

import scala.annotation.nowarn

object codecs {
  @nowarn implicit private val config: Configuration = Configuration.default

  implicit def idCodec[A: IdConverter]: Codec[A] =
    Codec.from(
      Decoder.decodeLong.map(IdConverter[A].fromLong),
      Encoder.encodeLong.contramap(IdConverter[A].toLong)
    )

  implicit def pageCodec[A: Codec]: Codec[Page[A]] = deriveConfiguredCodec

  implicit val userCodec: Codec[UserDTO]            = deriveConfiguredCodec
  implicit val postCodec: Codec[PostDTO]            = deriveConfiguredCodec
  implicit val commentCodec: Codec[CommentDTO]      = deriveConfiguredCodec
  implicit val tokenCodec: Codec[IdToken]           = deriveConfiguredCodec
  implicit val linkCodec: Codec[Link]               = deriveUnwrappedCodec
  implicit val deleteHashCodec: Codec[DeleteHash]   = deriveUnwrappedCodec
  implicit val base64ImageCodec: Codec[Base64Image] = deriveConfiguredCodec
  implicit val imageCodec: Codec[Image]             = deriveConfiguredCodec
  implicit val videoCodec: Codec[Video]             = deriveConfiguredCodec

  implicit val epochLocalDateTime: Decoder[LocalDateTime] = Decoder.decodeLong.map(LocalDateTime.ofEpochSecond(_, 0, ZoneOffset.UTC))

}
