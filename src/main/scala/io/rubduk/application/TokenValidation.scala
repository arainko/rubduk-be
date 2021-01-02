package io.rubduk.application

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import io.rubduk.config.AppConfig.AuthConfig
import io.rubduk.domain.TokenValidation
import io.rubduk.domain.errors.ValidationError
import io.rubduk.domain.models.auth.{IdToken, TokenUser}
import shapeless.syntax.typeable.typeableOps
import cats.implicits._
import zio.{Has, URLayer, ZIO, ZLayer}

import java.util.Collections
import scala.util.Try

object TokenValidation {

  trait Service {
    def validateToken(token: IdToken): Either[ValidationError, TokenUser]
  }

  def validateToken(idToken: IdToken): ZIO[TokenValidation, ValidationError, TokenUser] =
    ZIO.access[TokenValidation](_.get.validateToken(idToken)).flatMap(ZIO.fromEither(_))

  val googleOAuth2: URLayer[Has[AuthConfig], TokenValidation] = ZLayer.fromService { config =>
    new Service {
      private[this] val httpTransport = new NetHttpTransport()
      private[this] val jsonFactory   = JacksonFactory.getDefaultInstance
      private[this] val verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
        .setAudience(Collections.singletonList(config.clientId))
        .build()

      private def suppressedFallback(idToken: IdToken): Either[ValidationError, TokenUser] = {
        val token = idToken.token.split('-').toList
        (token.headOption, token.tail.headOption, token.tail.tail.headOption)
          .mapN(TokenUser)
          .toRight(ValidationError("Couldn't construct a token user from the provided mock token"))
      }

      override def validateToken(token: IdToken): Either[ValidationError, TokenUser] = {

        val verifiedToken = Try(verifier.verify(token.token)).toOption
          .flatMap(Option.apply)
          .map(_.getPayload)

        if (config.suppress)
          suppressedFallback(token)
        else
          for {
            _ <- verifiedToken.toRight(ValidationError("Invalid token"))
            email <-
              verifiedToken
                .map(_.getEmail)
                .toRight(ValidationError("Cannot read email from token."))
            lastName <-
              verifiedToken
                .flatMap(token => Option(token.get("family_name")).flatMap(_.cast[String]))
                .toRight(ValidationError("Cannot read firstname from token."))
            firstName <-
              verifiedToken
                .flatMap(token => Option(token.get("given_name")).flatMap(_.cast[String]))
                .toRight(ValidationError("Cannot read lastname from token."))
          } yield TokenUser(email, firstName, lastName)
      }
    }
  }
}
