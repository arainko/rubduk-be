package io.rubduk.domain.services

import io.rubduk.domain.MediaReadRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases.RowCount
import io.rubduk.domain.models.common.{Limit, Offset, Page}
import io.rubduk.domain.models.media.{MediaFilter, Medium}
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.services.MediaReadServiceLive
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object MediaReadService {

  trait Service {

    def getAll(
      offset: Offset,
      limit: Limit,
      filters: BoolAlgebra[MediaFilter]
    ): IO[ServerError, Seq[Medium]]

    def getPaginated(
      offset: Offset,
      limit: Limit,
      filters: BoolAlgebra[MediaFilter]
    ): IO[ServerError, Page[Medium]]

    def count(filters: BoolAlgebra[MediaFilter]): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, MediaReadRepository] =
    ZLayer.fromFunction(new MediaReadServiceLive(_))
}
