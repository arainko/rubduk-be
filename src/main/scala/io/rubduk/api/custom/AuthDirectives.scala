package io.rubduk.api.custom

import akka.http.javadsl.server.Rejections
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{optionalHeaderValueByName, provide, reject}
import io.rubduk.infrastructure.models.IdToken

object AuthDirectives {

  def idToken: Directive1[IdToken] =
    optionalHeaderValueByName("Authorization").flatMap { header =>
      header
        .map(IdToken)
        .map(provide)
        .getOrElse(reject(Rejections.authorizationFailed))
    }

}
