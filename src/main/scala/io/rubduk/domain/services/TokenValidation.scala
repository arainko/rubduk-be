package io.rubduk.domain.services

import java.util.Collections

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import io.rubduk.config._
import io.rubduk.domain.TokenValidation
import io.rubduk.domain.errors.ValidationError
import io.rubduk.infrastructure.models.{IdToken, TokenUser}
import zio.{Has, URLayer, ZIO, ZLayer}

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

      override def validateToken(token: IdToken): Either[ValidationError, TokenUser] = {
        import shapeless.syntax.typeable._

        val verifiedToken = Try(verifier.verify(token.token)).toOption
          .flatMap(Option.apply)
          .map(_.getPayload)
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
