package io.rubduk.domain.repositories

import io.rubduk.domain.PostRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.post._
import io.rubduk.infrastructure.repositories.PostRepositoryLive
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object PostRepository {

  trait Service {
    def getById(postId: PostId): IO[ServerError, Option[PostRecord]]

    def getAllPaginated(
      offset: Offset,
      limit: Limit,
      filters: Seq[PostFilter]
    ): IO[ServerError, Page[PostRecord]]

    def getAll(offset: Offset, limit: Limit, filters: Seq[PostFilter]): IO[ServerError, Seq[PostRecord]]

    def countFiltered(filters: Seq[PostFilter]): IO[ServerError, RowCount]

    def insert(post: PostRecord): IO[ServerError, PostId]

    def update(postId: PostId, contents: String): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, PostRepository] = ZLayer.fromFunction { database =>
    new PostRepositoryLive(database)
  }
}
