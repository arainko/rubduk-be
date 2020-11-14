package io.rubduk

import java.time.{ LocalDate, OffsetDateTime }

import cats.implicits.catsSyntaxOptionId
import com.typesafe.config.ConfigFactory
import io.rubduk.config.AppConfig
import io.rubduk.domain.repositories.{ PostRepository, UserRepository }
import io.rubduk.infrastructure.models.{ Limit, Offset, UserDAO }
import slick.interop.zio.DatabaseProvider
import zio.{ UIO, ZIO, ZLayer }
import zio.config.typesafe.TypesafeConfig
import zio.test.Assertion.{ equalTo, isSome }
import zio.test.environment.TestEnvironment
import zio.test.{ testM, DefaultRunnableSpec, ZSpec }
import zio.test.assert

object ServiceTest extends DefaultRunnableSpec {

  val rawConfig   = ConfigFactory.load.resolve
  val configLayer = TypesafeConfig.fromTypesafeConfig(rawConfig, AppConfig.descriptor)

  // using raw config since it's recommended and the simplest to work with slick
  val dbConfigLayer  = ZIO(rawConfig.getConfig("db")).toLayer
  val dbBackendLayer = ZLayer.succeed(slick.jdbc.PostgresProfile.backend)
  val repositoryLayer =
    (dbConfigLayer ++ dbBackendLayer) >>>
      DatabaseProvider.live >>>
      PostRepository.live ++ UserRepository.live

  val cos = testM("repo test") {
    for {
      id <- UserRepository.insert(
             UserDAO(
               None,
               "Test",
               None,
               "asdads",
               LocalDate.parse("2020-01-01").some,
               OffsetDateTime.parse("2020-11-22T22:06:08.211767+01")
             )
           )
      fetched <- UserRepository.getById(id).tap(user => UIO(println(user)))
//      test = OffsetDateTime.parse("2020-11-22T22:06:08.211767+01")
    } yield assert(fetched)(isSome)
  }

  override def spec: ZSpec[TestEnvironment, Any] = cos.provideCustomLayer(repositoryLayer.orDie)
}
