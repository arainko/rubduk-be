package io.rubduk.domain.services

import java.util.UUID

import io.rubduk.config.AppConfig.ImgurConfig
import io.rubduk.domain.MediaApi
import io.rubduk.domain.errors.ThirdPartyError
import io.rubduk.infrastructure.models.media.{Base64Image, ImageData, ImgurImageResponse}
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import sttp.client3.circe._
import zio.config.ZConfig
import zio.macros.accessible
import zio.{IO, URLayer, ZIO, ZLayer}

@accessible
object MediaApi {

  trait Service {
    def uploadImage(image: Base64Image): IO[ThirdPartyError, ImageData]
  }

  val imgur: URLayer[ZConfig[ImgurConfig] with SttpClient, MediaApi] =
    ZLayer.fromServices[ImgurConfig, SttpClient.Service, Service] { (config, sttpClient) =>
      new Service {
        import io.rubduk.api.serializers.codecs._

        private def uploadImageRequest(image: Base64Image) =
          basicRequest
            .header("Authorization", s"Client-ID ${config.clientId}")
            .post(uri"${config.baseApiUrl}/image")
            .body(("image", image.value), ("name", UUID.randomUUID.toString))
            .response(asJson[ImgurImageResponse])

        override def uploadImage(image: Base64Image): IO[ThirdPartyError, ImageData] =
          for {
            response <- sttpClient.send(uploadImageRequest(image)).mapError(ThirdPartyError)
            body <- ZIO.fromEither(response.body).mapError(ThirdPartyError)
          } yield body.data
      }
    }

}
