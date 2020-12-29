package io.rubduk.api

import akka.http.javadsl.server.Rejections
import akka.http.scaladsl.server.{Directive1, PathMatcher1}
import akka.http.scaladsl.server.Directives.{optionalHeaderValueByName, provide, reject}
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.typeclasses.IdConverter
import akka.http.scaladsl.server.Directives._

object directives {

  def idToken: Directive1[IdToken] =
    optionalHeaderValueByName("Authorization").flatMap { header =>
      header
        .map(IdToken)
        .map(provide)
        .getOrElse(reject(Rejections.authorizationFailed))
    }

  def Id[A: IdConverter]: PathMatcher1[A] = LongNumber.map(IdConverter[A].fromLong)
}
