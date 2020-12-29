package io.rubduk.domain.repositories

import io.rubduk.domain.MediaRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.media.{MediumId, MediumInRecord, MediumRecord}
import io.rubduk.infrastructure.repositories.MediaRepositoryLive
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}
import io.rubduk.domain.models.aliases._

@accessible
object MediaRepository {

  trait Service {
    def getById(mediumId: MediumId): IO[ServerError, Option[MediumRecord]]
    def insert(medium: MediumInRecord): IO[ServerError, MediumId]
    def delete(mediumId: MediumId): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, MediaRepository] = ZLayer.fromFunction(new MediaRepositoryLive(_))
}
