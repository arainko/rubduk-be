package io.rubduk.domain.services

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import cats.implicits.catsSyntaxEitherId
import io.rubduk.config.AppConfig.ImgurConfig
import io.rubduk.domain.Media
import io.rubduk.domain.errors.ThirdPartyError
import io.rubduk.infrastructure.models.media.{Base64Image, Image}
import zio.config.ZConfig
import zio.macros.accessible
import zio._

@accessible
object Media {

  trait Service {
    def uploadImage(image: Base64Image): IO[ThirdPartyError, Image]
  }

  val imgur: URLayer[ZConfig[ImgurConfig] with Has[ActorSystem], Media] =
    ZLayer.fromServices[ImgurConfig, ActorSystem, Service] { (config, system) =>
      new Service {
        import io.circe.parser._
        import io.rubduk.api.serializers.codecs._

        implicit private val sys: ActorSystem = system
        private val baseUrl                   = config.baseApiUrl

        override def uploadImage(image: Base64Image): IO[ThirdPartyError, Image] =
          for {
            parsedHeader <- ZIO.fromEither {
              HttpHeader.parse("Authorization", s"Client-ID ${config.clientId}") match {
                case ParsingResult.Ok(header, _) => header.asRight
                case ParsingResult.Error(error)  => error.asLeft
              }
            }.mapError(errors => ThirdPartyError(errors.summary))
            requestEntity = FormData(
              BodyPart("image", image.value),
              BodyPart("name", UUID.randomUUID.toString)
            )
            response <- ZIO.fromFuture { implicit ec =>
              Http().singleRequest {
                Post(s"$baseUrl/image", requestEntity).addHeader(parsedHeader)
              }
            }.mapError(error => ThirdPartyError(error.getMessage))
            jsonResponse <- ZIO.fromFuture(implicit ec => Unmarshaller.stringUnmarshaller(response.entity))
              .map(parse)
              .flatMap(ZIO.fromEither(_))
              .orElseFail(ThirdPartyError("err"))
            _ = println(jsonResponse)
            parsedResponse <- ZIO.fromOption(jsonResponse.hcursor.downField("data").focus)
              .orElseFail(ThirdPartyError("err"))
            _ = println(parsedResponse)
            image <- ZIO.fromEither(parsedResponse.as[Image])
              .tapError(err => ZIO.succeed(println(err)))
              .orElseFail(ThirdPartyError("err"))
            _ = println(image)
          } yield image
      }
    }

}
