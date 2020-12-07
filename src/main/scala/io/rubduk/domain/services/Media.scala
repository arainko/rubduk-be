package io.rubduk.domain.services

import java.util.UUID

import io.circe.Json
import io.rubduk.config.AppConfig.ImgurConfig
import io.rubduk.domain.Media
import io.rubduk.domain.errors.ThirdPartyError
import io.rubduk.infrastructure.models.media.{Base64Image, Link}
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import sttp.client3.circe._
import zio.config.ZConfig
import zio.macros.accessible
import zio.{IO, URLayer, ZIO, ZLayer}

@accessible
object Media {

  trait Service {
    def uploadImage(image: Base64Image): IO[ThirdPartyError, Link]
  }

  val imgur: URLayer[ZConfig[ImgurConfig] with SttpClient, Media] =
    ZLayer.fromServices[ImgurConfig, SttpClient.Service, Service] { (config, sttpClient) =>
      new Service {

        private def uploadImageRequest(image: Base64Image) =
          basicRequest
            .header("Authorization", s"Client-ID ${config.clientId}")
            .post(uri"${config.baseApiUrl}/image")
            .body(("image", image.value), ("name", UUID.randomUUID.toString))
            .response(asJson[Json])

        override def uploadImage(image: Base64Image): IO[ThirdPartyError, Link] =
          for {
            response <- sttpClient.send(uploadImageRequest(image)).mapError(ThirdPartyError)
            body <- ZIO.fromEither(response.body).mapError(ThirdPartyError)
            maybeLink = body.hcursor.downField("data").get[String]("link")
            link <- ZIO.fromEither(maybeLink).mapError(error => ThirdPartyError(error.message))
          } yield Link(link)
      }
    }

}
