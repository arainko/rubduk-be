package io.rubduk.domain.repositories

import io.rubduk.domain.MediaReadRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.live.MediaReadRepositoryLive
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.models.media.Medium
import io.rubduk.infrastructure.models.{Limit, Offset, Page, RowCount}
import io.rubduk.infrastructure.tables.Media
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object MediaReadRepository {

  trait Service {

    def getAll(
      offset: Offset,
      limit: Limit,
      filters: Seq[Filter[Media.Schema]]
    ): IO[ServerError, Seq[Medium]]

    def getPaginated(
      offset: Offset,
      limit: Limit,
      filters: Seq[Filter[Media.Schema]]
    ): IO[ServerError, Page[Medium]]

    def count(filters: Seq[Filter[Media.Schema]]): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, MediaReadRepository] =
    ZLayer.fromFunction(new MediaReadRepositoryLive(_))
}