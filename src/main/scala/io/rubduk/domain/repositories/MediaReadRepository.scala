package io.rubduk.domain.repositories

import io.rubduk.domain.MediaReadRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.media.{MediaFilter, Medium}
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.repositories.MediaReadRepositoryLive
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object MediaReadRepository {

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
    ZLayer.fromFunction(new MediaReadRepositoryLive(_))
}
