package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.infrastructure.Filter.FilterOps
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.typeclasses.IdConverter._
import io.rubduk.domain.models._
import io.rubduk.infrastructure.Filter
import io.rubduk.infrastructure.tables.Posts
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class PostRepositoryLive(env: DatabaseProvider) extends PostRepository.Service {

  override def getById(postId: PostId): IO[ServerError, Option[PostDAO]] =
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
    filters: Seq[Filter[Posts.Schema]]
  ): IO[ServerError, Page[PostDAO]] =
    getAll(offset, limit, filters).zipPar(countFiltered(filters)).map {
      case (posts, postCount) => Page(posts, postCount)
    }

  override def getAll(offset: Offset, limit: Limit, filters: Seq[Filter[Posts.Schema]]): IO[ServerError, Seq[PostDAO]] =
    ZIO
      .fromDBIO {
        Posts.table
          .filteredBy(filters)
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def countFiltered(filters: Seq[Filter[Posts.Schema]]): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Posts.table.filteredBy(filters).length.result
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(post: PostDAO): IO[ServerError, PostId] =
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
