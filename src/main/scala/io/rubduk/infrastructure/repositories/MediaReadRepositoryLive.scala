package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.media._
import io.rubduk.domain.repositories.MediaReadRepository
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.filters.syntax._
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.tables.Media
import slick.interop.zio._
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class MediaReadRepositoryLive(env: DatabaseProvider) extends MediaReadRepository.Service {

  override def getAll(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[MediaFilter]
  ): IO[ServerError, Seq[Medium]] =
    ZIO
      .fromDBIO {
        Media.table
          .filteredBy(filters)
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .bimap(ServerError, _.map(_.toDomain))
      .provide(env)

  override def getPaginated(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[MediaFilter]
  ): IO[ServerError, Page[Medium]] =
    getAll(offset, limit, filters)
      .zipPar(count(filters))
      .map((Page.apply[Medium] _).tupled)

  override def count(filters: BoolAlgebra[MediaFilter]): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Media.table.filteredBy(filters).length.result
      }
      .mapError(ServerError)
      .provide(env)
}
