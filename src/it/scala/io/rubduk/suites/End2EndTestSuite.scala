package io.rubduk.suites

import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import com.typesafe.config.ConfigFactory
import io.rubduk.api.routes.Api
import io.rubduk.config.AppConfig
import io.rubduk.domain.repositories._
import slick.interop.zio.DatabaseProvider
import slick.jdbc.PostgresProfile
import zio.config.typesafe.TypesafeConfig
import zio.test.DefaultRunnableSpec
import zio._
import io.rubduk.domain.services._
import akka.actor.ActorSystem
import io.rubduk.config.AppConfig.AuthConfig
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.clock.Clock

trait End2EndTestSuite extends DefaultRunnableSpec with TestFrameworkInterface with RouteTest {
  def failTest(msg: String): Nothing = throw new Exception(msg)

  def testExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: Throwable => throw e
    }
}

object End2EndTestSuite {

  private val rawConfig   = ConfigFactory.load.resolve
  private val configLayer = TypesafeConfig.fromTypesafeConfig(rawConfig, AppConfig.descriptor)

  // using raw config since it's recommended and the simplest to work with slick
  val dbConfigLayer  = ZIO(rawConfig.getConfig("db")).toLayer
  val dbBackendLayer = ZLayer.succeed(PostgresProfile.backend)

  val repositoryLayer =
    (dbConfigLayer ++ dbBackendLayer) >>>
      DatabaseProvider.live >>>
      (
        PostRepository.live ++
          UserRepository.live ++
          CommentRepository.live ++
          MediaReadService.live ++
          MediaRepository.live ++
          FriendRequestRepository.live ++
          FriendRequestService.live ++
          LikeRepository.live
      )

  // narrowing down to the required part of the config to ensure separation of concerns
  val apiConfigLayer       = configLayer.map(c => Has(c.get.api))
  val tokenValidationLayer = ZLayer.succeed {
    AuthConfig("TEST", true)
  } >>> io.rubduk.application.TokenValidation.googleOAuth2

  val actorSystemLayer = ZLayer.fromManaged {
    ZManaged.make(ZIO(ActorSystem("rubduk-system")))(s => ZIO.fromFuture(_ => s.terminate()).either)
  }

  val mediaLayer = configLayer.map(c => Has(c.get.imgur)) ++
    AsyncHttpClientZioBackend.layer() >>>
    io.rubduk.application.MediaApi.imgur

  val apiLayer =
    apiConfigLayer >+>
      tokenValidationLayer >+>
      repositoryLayer >+>
      mediaLayer >+>
      Clock.live >+> Api.live

  val testLayer = repositoryLayer ++ apiLayer
}
