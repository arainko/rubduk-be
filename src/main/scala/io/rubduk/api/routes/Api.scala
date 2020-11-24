package io.rubduk.api.routes

import akka.http.interop.{HttpServer, ZIOSupport}
import akka.http.scaladsl.server.Route
import io.rubduk.api.Api
import io.rubduk.domain.{CommentRepository, PostRepository, TokenValidation, UserRepository}
import zio.config.ZConfig
import zio.{URIO, ZIO, ZLayer}
import akka.http.scaladsl.server.Directives._

object Api {

  trait Service extends ZIOSupport {
    def routes: Route
  }

  val live
    : ZLayer[ZConfig[HttpServer.Config] with PostRepository with UserRepository with CommentRepository with TokenValidation, Nothing, Api] =
    ZLayer.fromFunction { env =>
      new Service {
        def routes: Route = PostsApi(env) ~ UsersApi(env)
      }
    }

  // accessors
  val routes: URIO[Api, Route] = ZIO.access[Api](a => Route.seal(a.get.routes))
}
