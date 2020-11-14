package io.rubduk.api

import io.rubduk.Entities
import io.rubduk.suites.End2EndTestSuite
import zio.blocking.effectBlocking
import zio.test.Assertion.equalTo
import zio.test.ZSpec
import zio.test._
import zio.test.environment.TestEnvironment

object UserApiEndToEndSpec extends End2EndTestSuite {
  import io.rubduk.api.serializers.codecs._

  val costam = suite("asd")(
    testM("costam") {
      // given
      val userToInsert = Entities.user
      for {
        routeAssertions <- effectBlocking {
          Get("/api/users")
        }
      }
    }
  )

  override def spec: ZSpec[TestEnvironment, Any] = costam
}
