package io.rubduk.domain.repositories

import io.rubduk.domain.PostRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.infrastructure.repositories.PostRepositoryLive
import io.rubduk.domain.models._
import io.rubduk.infrastructure.Filter
import io.rubduk.infrastructure.tables.Posts
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object PostRepository {

  trait Service {
    def getById(postId: PostId): IO[ServerError, Option[PostDAO]]

    def getAllPaginated(
      offset: Offset,
      limit: Limit,
      filters: Seq[Filter[Posts.Schema]]
    ): IO[ServerError, Page[PostDAO]]

    def getAll(offset: Offset, limit: Limit, filters: Seq[Filter[Posts.Schema]]): IO[ServerError, Seq[PostDAO]]

    def countFiltered(filters: Seq[Filter[Posts.Schema]]): IO[ServerError, RowCount]

    def insert(post: PostDAO): IO[ServerError, PostId]

    def update(postId: PostId, contents: String): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, PostRepository] = ZLayer.fromFunction { database =>
    new PostRepositoryLive(database)
  }
}
