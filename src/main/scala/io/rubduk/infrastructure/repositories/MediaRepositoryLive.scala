package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.media._
import io.rubduk.domain.repositories.MediaRepository
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.tables.Media
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}
import io.rubduk.domain.models.aliases._

class MediaRepositoryLive(env: DatabaseProvider) extends MediaRepository.Service {

  override def getById(mediumId: MediumId): IO[ServerError, Option[MediumRecord]] =
    ZIO
      .fromDBIO {
        Media.table
          .filter(_.id === mediumId)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(medium: MediumInRecord): IO[ServerError, MediumId] =
    ZIO
      .fromDBIO {
        Media.table
          .returning(Media.table.map(_.id)) += medium.toOutRecord(MediumId(0))
      }
      .mapError(ServerError)
      .provide(env)

  override def delete(mediumId: MediumId): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Media.table.filter(_.id === mediumId).delete
      }
      .mapError(ServerError)
      .provide(env)
}
