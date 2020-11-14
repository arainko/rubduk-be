package io.rubduk.domain.repositories.live

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.typeclasses.IdConverter._
import io.rubduk.infrastructure.models._
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

  override def getAllPaginated(offset: Offset, limit: Limit): IO[ServerError, Page[PostDAO]] =
    getAll(offset, limit).zipPar(count).map {
      case (posts, postCount) => Page(posts, postCount)
    }

  override def getAll(offset: Offset, limit: Limit): IO[ServerError, Seq[PostDAO]] =
    ZIO
      .fromDBIO {
        Posts.table
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def count: IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Posts.table.length.result
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
