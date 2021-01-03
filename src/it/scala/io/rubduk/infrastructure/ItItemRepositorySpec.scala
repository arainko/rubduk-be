package io.rubduk.infrastructure

import io.rubduk.Entities
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.flyway.FlywayProvider
import io.rubduk.suites.ITSpec.ITSpec
import zio.test.Assertion._
import zio.test.{suite, testM, _}
import zio.test.TestAspect.before

object ItItemRepositorySpec extends ITSpec(Some("items")) {

  val migrateDbSchema =
    FlywayProvider.flyway
      .flatMap(_.migrate)
      .toManaged_

  migrateDbSchema.useNow
  val spec: ITSpec =
    suite("Item Repository")(
      testM("Should not allow to add wrong data to db") {
        val user = Entities.user.toDAO
        for {
          insertedId <- UserRepository.insert(user)
          _ <- migrateDbSchema.useNow
          fetched <- UserRepository.getById(insertedId)
          _ = println(fetched)
        } yield assert(true)(equalTo(true))
      },
    ) @@ before(FlywayProvider.flyway.flatMap(_.migrate).orDie)
}