package io.rubduk.api

import akka.http.interop._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.Marshalling._
import akka.http.scaladsl.server.Route
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.UserError.UserNotFound
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.domain.UserRepository
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.services.UserService
import io.rubduk.infrastructure.models.{Limit, Offset, Page, RowCount, UserDAO, UserId}
import zio._
import zio.config.ZConfig

object Api {

  trait Service {
    def routes: Route
  }

  val live: ZLayer[ZConfig[HttpServer.Config], Nothing, Api] = ZLayer.fromFunction { env =>
    new Service with ZIOSupport {

      def routes: Route = placeholderRoute

//      implicit val domainErrorResponse: ErrorResponse[Throwable] = {
//        case _: Throwable => HttpResponse(StatusCodes.NotFound)
//      }

      val placeholderRoute: Route = path("test") {
        complete {
          "PLACEHOLDER"
        }
      }
    }
  }

  // accessors
  val routes: URIO[Api, Route] = ZIO.access[Api](a => Route.seal(a.get.routes))
}
