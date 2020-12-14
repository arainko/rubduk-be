package io.rubduk.domain.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.infrastructure.models.media.{MediumId, MediumInRecord, MediumOutRecord}
import zio.IO
import zio.macros.accessible

@accessible
object MediaRepository {
  trait Service {
    def getById(mediumId: MediumId): IO[ServerError, Option[MediumOutRecord]]
    def insert(medium: MediumInRecord): IO[ServerError, MediumId]
  }

//  val live: URLayer[DatabaseProvider, MediaRepository]
}
