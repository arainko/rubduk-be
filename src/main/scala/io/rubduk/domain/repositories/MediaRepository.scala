package io.rubduk.domain.repositories

import io.rubduk.domain.MediaRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.live.MediaRepositoryLive
import io.rubduk.infrastructure.models.RowCount
import io.rubduk.infrastructure.models.media.{MediumId, MediumInRecord, MediumRecord}
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZLayer}
import zio.macros.accessible

@accessible
object MediaRepository {

  trait Service {
    def getById(mediumId: MediumId): IO[ServerError, Option[MediumRecord]]
    def insert(medium: MediumInRecord): IO[ServerError, MediumId]
    def delete(mediumId: MediumId): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, MediaRepository] = ZLayer.fromFunction(new MediaRepositoryLive(_))
}
