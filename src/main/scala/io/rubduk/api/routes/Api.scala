package io.rubduk.api.routes

import akka.http.interop.{HttpServer, ZIOSupport}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.rubduk.api.Api
import io.rubduk.domain._
import zio.clock.Clock
import zio.config.ZConfig
import zio.{Has, URIO, ZIO, ZLayer}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import io.rubduk.domain.services.FriendRequestService

object Api {

  trait Service extends ZIOSupport {
    def routes: Route
  }

  val live: ZLayer[ZConfig[
    HttpServer.Config
  ] with PostRepository
    with UserRepository
    with CommentRepository
    with TokenValidation
    with MediaApi
    with MediaReadRepository
    with MediaRepository
    with Clock
    with FriendRequestRepository
    with Has[FriendRequestService.Service],
    Nothing, Api] =
    ZLayer.fromFunction { env =>
      new Service {
        def routes: Route =
          cors() {
            PostsApi(env) ~ UsersApi(env) ~ new FriendRequestApi(env).routes
          }
      }
    }

  // accessors
  val routes: URIO[Api, Route] = ZIO.access[Api](a => Route.seal(a.get.routes))
}
