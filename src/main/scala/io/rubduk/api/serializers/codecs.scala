package io.rubduk.api.serializers

import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder, Encoder}
import io.rubduk.domain.models._
import io.rubduk.domain.models.media.{Base64Image, ImageData, ImgurImageResponse, Link, MediumDTO}
import io.rubduk.domain.typeclasses.IdConverter

object codecs {

  implicit def idCodec[A: IdConverter]: Codec[A] =
    Codec.from(
      Decoder.decodeLong.map(IdConverter[A].fromLong),
      Encoder.encodeLong.contramap(IdConverter[A].toLong)
    )

  implicit def pageCodec[A: Codec]: Codec[Page[A]] = deriveCodec

  implicit val userCodec: Codec[UserDTO]       = deriveCodec
  implicit val postCodec: Codec[PostDTO]       = deriveCodec
  implicit val commentCodec: Codec[CommentDTO] = deriveCodec
  implicit val tokenCodec: Codec[IdToken]      = deriveCodec

  implicit val linkCodec: Codec[Link]                        = deriveUnwrappedCodec
  implicit val imgurDataCodec: Codec[ImageData]              = deriveCodec
  implicit val imgurResponseCodec: Codec[ImgurImageResponse] = deriveCodec
  implicit val base64ImageCodec: Codec[Base64Image]          = deriveCodec
  implicit val mediumCodec: Codec[MediumDTO]          = deriveCodec

}
