package io.rubduk.api.routes

import akka.http.interop.{HttpServer, ZIOSupport}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directives, Route}
import io.rubduk.api.Api
import io.rubduk.domain.services.Media
import io.rubduk.domain.{CommentRepository, Media, PostRepository, TokenValidation, UserRepository}
import io.rubduk.infrastructure.models.Base64Image
import zio.config.ZConfig
import zio.{URIO, ZIO, ZLayer}

object Api {

  trait Service extends ZIOSupport {
    def routes: Route
  }

  val live: ZLayer[ZConfig[
    HttpServer.Config
  ] with PostRepository with UserRepository with CommentRepository with TokenValidation with Media, Nothing, Api] =
    ZLayer.fromFunction { env =>
      new Service {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        import io.circe.generic.auto._

        def routes: Route =
          PostsApi(env) ~ UsersApi(env) ~ path("image") {
            post {
              entity(Directives.as[Base64Image]) { image =>
                complete {
                  Media.uploadImage(image).provide(env)
                }
              }
            }
          }
      }
    }

  // accessors
  val routes: URIO[Api, Route] = ZIO.access[Api](a => Route.seal(a.get.routes))
}
