package io.rubduk

import akka.actor.ActorSystem
import akka.http.interop._
import akka.http.scaladsl.server.Route
import com.typesafe.config.{Config, ConfigFactory}
import io.rubduk.api._
import io.rubduk.api.routes.Api
import io.rubduk.application.{MediaApi, TokenValidation}
import io.rubduk.config.AppConfig
import io.rubduk.domain.repositories._
import io.rubduk.domain.services.{FriendRequestService, MediaReadService}
import io.rubduk.infrastructure.flyway.FlywayProvider
import slick.interop.zio.DatabaseProvider
import slick.jdbc.PostgresProfile
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.clock.Clock
import zio.config.typesafe.TypesafeConfig
import zio.console._
import zio.duration.durationInt

object Boot extends App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(rawConfig => program.provideCustomLayer(prepareEnvironment(rawConfig)))
      .exitCode

  private val program: RIO[HttpServer with FlywayProvider with ZEnv, Unit] = {
    val startHttpServer =
      HttpServer.start.tapM(_ => putStrLn("Server online."))

    val migrateDbSchema =
      FlywayProvider.flyway
        .flatMap(_.migrate)
        .retry(Schedule.exponential(200.millis))
        .flatMap(res => putStrLn(s"Flyway migration completed with: $res"))
        .toManaged_

    (startHttpServer *> migrateDbSchema).useForever
  }

  private def prepareEnvironment(rawConfig: Config): TaskLayer[HttpServer with FlywayProvider] = {
    val configLayer = TypesafeConfig.fromTypesafeConfig(rawConfig, AppConfig.descriptor)

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
    val tokenValidationLayer = configLayer.map(c => Has(c.get.auth)) >>> TokenValidation.googleOAuth2
    val actorSystemLayer = ZLayer.fromManaged {
      ZManaged.make(ZIO(ActorSystem("rubduk-system")))(s => ZIO.fromFuture(_ => s.terminate()).either)
    }

    val mediaLayer = configLayer.map(c => Has(c.get.imgur)) ++
      AsyncHttpClientZioBackend.layer() >>>
      MediaApi.imgur

    val flywayLayer: TaskLayer[FlywayProvider] =
      dbConfigLayer >>> FlywayProvider.live

    // Disabled for now
//    val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
//      val logFormat = "[correlation-id = %s] %s"
//      val correlationId = LogAnnotation.CorrelationId.render(
//        context.get(LogAnnotation.CorrelationId)
//      )
//      logFormat.format(correlationId, message)
//    }

    val apiLayer: TaskLayer[Api] =
      apiConfigLayer ++
        tokenValidationLayer ++
        repositoryLayer ++
        mediaLayer ++
        Clock.live >>> Api.live

    val routesLayer: ZLayer[Api, Nothing, Has[Route]] =
      ZLayer.fromService(_.routes)

    val serverLayer = (actorSystemLayer ++ apiConfigLayer ++ (apiLayer >>> routesLayer)) >>> HttpServer.live

    serverLayer ++ flywayLayer
  }
}
