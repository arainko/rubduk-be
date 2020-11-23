package io.rubduk.domain.services

import java.util.Collections

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import io.rubduk.config._
import io.rubduk.domain.TokenValidationService
import zio.{Has, URLayer, ZIO, ZLayer}
import io.rubduk.domain.errors.ApplicationError._

final case class IdToken(value: String) extends AnyVal

final case class TokenUser(email: String, firstName: String, lastName: String)

object TokenValidationService {

  trait Service {
    def validateToken(token: IdToken): Either[ValidationError, TokenUser]
  }

  def validateToken(token: IdToken) = ZIO.access[Has[AuthConfig]](_.get.)

  val live: URLayer[Has[AuthConfig], TokenValidationService] = ZLayer.fromService { config =>
    new Service {
      import cats.implicits._

      private[this] val httpTransport = new NetHttpTransport()
      private[this] val jsonFactory   = JacksonFactory.getDefaultInstance
      private[this] val verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
        .setAudience(Collections.singletonList(config.clientId))
        .build()

      override def validateToken(token: IdToken): Either[ValidationError, TokenUser] = {
        import shapeless.syntax.typeable._
        val verifiedToken = Option(verifier.verify(token.value)).map(_.getPayload)
        val email = verifiedToken.map(_.getEmail)
          .toRight(ValidationError("Cannot read email from token."))
        val firstname = verifiedToken.flatMap(_.get("first_name").cast[String])
          .toRight(ValidationError("Cannot read firstname from token."))
        val lastName = verifiedToken.flatMap(_.get("given_name").cast[String])
          .toRight(ValidationError("Cannot read lastname from token."))
        (email, firstname, lastName).mapN(TokenUser)
      }
    }
  }
}
