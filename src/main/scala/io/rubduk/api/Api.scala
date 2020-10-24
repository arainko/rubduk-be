package io.rubduk.api

import akka.http.interop._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.rubduk.domain._
import io.rubduk.domain.errors.{DomainError, RepositoryError, ValidationError}
import zio._
import zio.config.ZConfig

object Api {

  trait Service {
    def routes: Route
  }

  val live: ZLayer[ZConfig[HttpServer.Config], Nothing, Api] = ZLayer.fromFunction { env =>
    new Service with ZIOSupport {

      def routes: Route = placeholderRoute

      implicit val domainErrorResponse: ErrorResponse[DomainError] = {
        case RepositoryError(_) => HttpResponse(StatusCodes.InternalServerError)
        case ValidationError(_) => HttpResponse(StatusCodes.BadRequest)
      }

      val placeholderRoute: Route = path("test") {
        complete("PLACEHOLDER")
      }
    }
  }

  // accessors
  val routes: URIO[Api, Route] = ZIO.access[Api](a => Route.seal(a.get.routes))
}
