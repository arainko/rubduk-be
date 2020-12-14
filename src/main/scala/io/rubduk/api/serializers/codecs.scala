package io.rubduk.api.serializers

import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder, Encoder}
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.models.media.{ImageData, ImgurImageResponse, Link}
import io.rubduk.infrastructure.typeclasses.IdConverter

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

}
