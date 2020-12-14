package io.rubduk.domain.repositories.live

import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.repositories.MediaRepository
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.media._
import io.rubduk.infrastructure.tables.Media
import io.rubduk.infrastructure.typeclasses.IdConverter._
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class MediaRepositoryLive(env: DatabaseProvider) extends MediaRepository.Service {

  override def getById(mediumId: MediumId): IO[ServerError, Option[MediumOutRecord]] =
    ZIO
      .fromDBIO {
        Media.table
          .filter(_.id === mediumId)
          .result
          .headOption
      }
      .bimap(ServerError, _.map(_.unsafeToOutRecord))
      .provide(env)

  override def insert(medium: MediumInRecord): IO[ServerError, MediumId] =
    ZIO
      .fromDBIO {
        Media.table.returning(Media.table.map(_.id)) += medium.toRecord
      }
      .mapError(ServerError)
      .provide(env)
}
