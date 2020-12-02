package io.rubduk.domain.services

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import io.rubduk.config.AppConfig.ImgurConfig
import io.rubduk.domain.Media
import io.rubduk.domain.errors.ThirdPartyError
import io.rubduk.infrastructure.models.{Base64Image, Link}
import zio.config.ZConfig
import zio.{Has, IO, URLayer, ZLayer}
import zio.macros.accessible
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.unmarshalling.Unmarshaller
import cats.implicits.catsSyntaxEitherId
import zio.ZIO

@accessible
object Media {

  trait Service {
    def uploadImage(image: Base64Image): IO[ThirdPartyError, Link]
  }

  val imgur: URLayer[ZConfig[ImgurConfig] with Has[ActorSystem], Media] =
    ZLayer.fromServices[ImgurConfig, ActorSystem, Service] { (config, system) =>
      new Service {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        import io.circe.parser._

        implicit private val sys: ActorSystem = system
        private val baseUrl                   = config.baseApiUrl

        override def uploadImage(image: Base64Image): IO[ThirdPartyError, Link] =
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
              val request = Post(s"$baseUrl/image", requestEntity).addHeader(parsedHeader)
              Http().singleRequest(request)
            }.mapError(error => ThirdPartyError(error.getMessage))
            stringResponse <- ZIO.fromFuture(implicit ec => Unmarshaller.stringUnmarshaller(response.entity))
              .mapError(error => ThirdPartyError(error.getMessage))
            parsedResponse <-
              ZIO
                .fromEither(parse(stringResponse))
                .mapError(error => ThirdPartyError(error.message))
          } yield Link(parsedResponse.hcursor.downField("data").downField("link").focus.getOrElse("Error lmao").toString)
      }
    }

}
