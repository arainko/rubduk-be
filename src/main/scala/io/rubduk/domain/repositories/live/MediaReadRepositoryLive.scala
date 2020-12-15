package io.rubduk.domain.repositories.live

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.repositories.MediaReadRepository
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.additional.Filter.FilterOps
import io.rubduk.infrastructure.models.media._
import io.rubduk.infrastructure.models.{Limit, Offset, Page, RowCount, media}
import io.rubduk.infrastructure.tables.Media
import slick.interop.zio._
import slick.interop.zio.syntax._
import Page._
import cats.syntax.functor._
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import zio.{IO, ZIO}

class MediaReadRepositoryLive(env: DatabaseProvider) extends MediaReadRepository.Service {

  override def getAll(
    offset: Offset,
    limit: Limit,
    filters: Seq[Filter[Media.Schema]]
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
    filters: Seq[Filter[Media.Schema]]
  ): IO[ServerError, Page[Medium]] =
    getAll(offset, limit, filters)
      .zipPar(count(filters))
      .map((Page.apply[Medium] _).tupled)

  override def count(filters: Seq[Filter[Media.Schema]]): IO[ServerError, RowCount] =
    ZIO.fromDBIO {
      Media.table.filteredBy(filters).length.result
    }
      .mapError(ServerError)
      .provide(env)
}
