package io.rubduk.suites

import com.typesafe.config.{Config, ConfigFactory}
import io.rubduk.domain.UserRepository
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.Postgres
import io.rubduk.infrastructure.Postgres.SchemaAwarePostgresContainer
import io.rubduk.infrastructure.flyway.FlywayProvider
import slick.interop.zio.DatabaseProvider
import zio.blocking.Blocking
import zio.duration.durationInt
import zio.logging._
import zio.logging.slf4j.Slf4jLogger
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{Has, Layer, ULayer, ZLayer}

import scala.jdk.CollectionConverters.MapHasAsJava

object ITSpec {
  type Postgres = Has[SchemaAwarePostgresContainer]
  type ITEnv    = TestEnvironment with FlywayProvider with Logging with UserRepository

  abstract class ITSpec(schema: Option[String] = None) extends RunnableSpec[ITEnv, Any] {
    type ITSpec = ZSpec[ITEnv, Any]

    override def aspects: List[TestAspect[Nothing, ITEnv, Nothing, Any]] =
      List(TestAspect.timeout(60.seconds))

    override def runner: TestRunner[ITEnv, Any] =
      TestRunner(TestExecutor.default(itLayer))

    val blockingLayer: Layer[Nothing, Blocking]       = Blocking.live

    val postgresLayer: ZLayer[Any, Nothing, Postgres] = blockingLayer >>> Postgres.postgres(schema)

    val dbLayer: ZLayer[
      Any with Postgres with Blocking,
      Nothing,
      TestEnvironment with FlywayProvider with Logging with UserRepository
    ] = {

      val config: ZLayer[Postgres, Nothing, Has[Config]] = ZLayer
        .fromService[SchemaAwarePostgresContainer, Config] { container =>
          ConfigFactory.parseMap(
            Map(
              "url"            -> container.jdbcUrl,
              "user"           -> container.username,
              "password"       -> container.password,
              "driver"         -> "org.postgresql.Driver",
              "connectionPool" -> "HikariCP",
              "numThreads"     -> 1,
              "queueSize"      -> 100
            ).asJava
          )
        }

      val dbProvider: ZLayer[Postgres with Any, Throwable, DatabaseProvider] =
        config ++ ZLayer.succeed(slick.jdbc.PostgresProfile.backend) >>> DatabaseProvider.live

      val flyWayProvider = config >>> FlywayProvider.live

      val postgresLayer = Postgres.postgres(schema)
      val blockingLayer = Blocking.live

      val containerDatabaseProvider: ZLayer[Blocking, Throwable, DatabaseProvider] =
        blockingLayer >>> postgresLayer >>> dbProvider

      val containerRepository: ZLayer[Blocking, Throwable, UserRepository] =
        (Logging.ignore ++ containerDatabaseProvider) >>> UserRepository.live

      val logging = Slf4jLogger.make { (context, message) =>
        val logFormat     = "[correlation-id = %s] %s"
        val correlationId = LogAnnotation.CorrelationId.render(
          context.get(LogAnnotation.CorrelationId)
        )
        logFormat.format(correlationId, message)
      }
      zio.test.environment.testEnvironment ++ flyWayProvider ++ logging ++ containerRepository
    }.orDie

    val itLayer: ULayer[ITEnv] = postgresLayer ++ blockingLayer >>> dbLayer
  }
}
