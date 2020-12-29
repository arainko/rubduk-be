package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.post._
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.filters.syntax._
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.tables.Posts
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class PostRepositoryLive(env: DatabaseProvider) extends PostRepository.Service {

  override def getById(postId: PostId): IO[ServerError, Option[PostRecord]] =
    ZIO
      .fromDBIO {
        Posts.table
          .filter(_.id === postId)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: Seq[PostFilter]
  ): IO[ServerError, Page[PostRecord]] =
    getAll(offset, limit, filters).zipPar(countFiltered(filters)).map {
      case (posts, postCount) => Page(posts, postCount)
    }

  override def getAll(offset: Offset, limit: Limit, filters: Seq[PostFilter]): IO[ServerError, Seq[PostRecord]] =
    ZIO
      .fromDBIO {
        Posts.table
          .filteredBy(filters.map(_.interpret))
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def countFiltered(filters: Seq[PostFilter]): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Posts.table.filteredBy(filters.map(_.interpret)).length.result
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(post: PostRecord): IO[ServerError, PostId] =
    ZIO
      .fromDBIO {
        Posts.table.returning(Posts.table.map(_.id)) += post
      }
      .mapError(ServerError)
      .provide(env)

  override def update(postId: PostId, contents: String): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Posts.table
          .filter(_.id === postId)
          .map(p => p.contents)
          .update(contents)
      }
      .mapError(ServerError)
      .provide(env)
}
