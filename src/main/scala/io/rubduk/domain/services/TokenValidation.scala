package io.rubduk.domain.services

import java.util.Collections

import cats.data.ValidatedNec
import cats.implicits.catsSyntaxOptionId
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import io.rubduk.config._
import io.rubduk.domain.TokenValidation
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.infrastructure.models.UserDTO
import zio.{Has, URLayer, ZIO, ZLayer}

final case class IdToken(token: String) extends AnyVal

final case class TokenUser(email: String, firstName: String, lastName: String) {
  def toDTO: UserDTO = UserDTO(None, firstName, lastName.some, email, None, None)
}

object TokenValidation {

  trait Service {
    def validateToken(token: IdToken): ValidatedNec[ValidationError, TokenUser]
  }

  def validateToken(idToken: IdToken): ZIO[TokenValidation, ValidationError, TokenUser] =
    ZIO.access[TokenValidation](_.get.validateToken(idToken).toEither)
      .flatMap(ZIO.fromEither(_))
      .mapError(accumulatedErrors => ValidationError(accumulatedErrors.map(_.message).reduce))

  val googleOAuth2: URLayer[Has[AuthConfig], TokenValidation] = ZLayer.fromService { config =>
    new Service {
      import cats.implicits._

      println(config)
      private[this] val httpTransport = new NetHttpTransport()
      private[this] val jsonFactory   = JacksonFactory.getDefaultInstance
      private[this] val verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
        .setAudience(Collections.singletonList(config.clientId))
        .build()

      override def validateToken(token: IdToken): ValidatedNec[ValidationError, TokenUser] = {
        import shapeless.syntax.typeable._

        val verifiedToken = Option(verifier.verify(token.token)).map(_.getPayload)
        val validToken = verifiedToken
          .toValidNec(ValidationError("Invalid token"))
        println(validToken)

        val email = verifiedToken
          .map(_.getEmail)
          .toValidNec(ValidationError("Cannot read email from token."))
        println(email)
        val lastName = verifiedToken
          .flatMap(token => Option(token.get("family_name123")).flatMap(_.cast[String]))
          .toValidNec(ValidationError("Cannot read firstname from token."))
        println(lastName)
        val firstName = verifiedToken
          .flatMap(token => Option(token.get("given_name")).flatMap(_.cast[String]))
          .toValidNec(ValidationError("Cannot read lastname from token."))
println(firstName)
         println(verifiedToken.flatMap(token => Option(token.get("random")).flatMap(_.cast[String])))

        (validToken, email, firstName, lastName).mapN {
          case (_, email, firstname, lastname) =>
            TokenUser(email, firstname, lastname)
        }
      }
    }
  }
}
