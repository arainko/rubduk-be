package io.rubduk

import akka.actor.ActorSystem
import akka.http.interop._
import akka.http.scaladsl.server.Route
import com.typesafe.config.{ Config, ConfigFactory }
import io.rubduk.api._
import io.rubduk.api.routes.Api
import io.rubduk.config.AppConfig
import io.rubduk.domain.repositories.{ CommentRepository, PostRepository, UserRepository }
import slick.interop.zio.DatabaseProvider
import slick.jdbc.PostgresProfile
import zio._
import zio.config.typesafe.TypesafeConfig
import zio.console._

object Boot extends App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(rawConfig => program.provideCustomLayer(prepareEnvironment(rawConfig)))
      .exitCode

  private val program: ZIO[HttpServer with Console, Throwable, Unit] =
    HttpServer.start.tapM(_ => putStrLn(s"Server online.")).useForever

  private def prepareEnvironment(rawConfig: Config): TaskLayer[HttpServer] = {
    val configLayer = TypesafeConfig.fromTypesafeConfig(rawConfig, AppConfig.descriptor)

    // using raw config since it's recommended and the simplest to work with slick
    val dbConfigLayer  = ZIO(rawConfig.getConfig("db")).toLayer
    val dbBackendLayer = ZLayer.succeed(PostgresProfile.backend)
    val repositoryLayer =
      (dbConfigLayer ++ dbBackendLayer) >>>
        DatabaseProvider.live >>>
        (PostRepository.live ++ UserRepository.live ++ CommentRepository.live)

    // narrowing down to the required part of the config to ensure separation of concerns
    val apiConfigLayer = configLayer.map(c => Has(c.get.api))

    val actorSystemLayer: TaskLayer[Has[ActorSystem]] = ZLayer.fromManaged {
      ZManaged.make(ZIO(ActorSystem("rubduk-system")))(s => ZIO.fromFuture(_ => s.terminate()).either)
    }

    // Disabled for now
//    val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
//      val logFormat = "[correlation-id = %s] %s"
//      val correlationId = LogAnnotation.CorrelationId.render(
//        context.get(LogAnnotation.CorrelationId)
//      )
//      logFormat.format(correlationId, message)
//    }

    val apiLayer: TaskLayer[Api] = apiConfigLayer ++ repositoryLayer >>> Api.live

    val routesLayer: ZLayer[Api, Nothing, Has[Route]] =
      ZLayer.fromService(_.routes)

    (actorSystemLayer ++ apiConfigLayer ++ (apiLayer >>> routesLayer)) >>> HttpServer.live
  }
}
