package io.rubduk.api.custom

import akka.http.javadsl.server.Rejections
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import io.rubduk.infrastructure.models.UserId

// Here until we get an actual auth implementation
object PlaceholderDirectives {
  def userId: Directive1[UserId] =
    optionalHeaderValueByName("User-Id").flatMap { header =>
      header
        .flatMap(_.toLongOption.map(UserId))
        .map(provide)
        .getOrElse(reject(Rejections.authorizationFailed))
    }
}
