package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.comment._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.post._
import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.filters.syntax._
import io.rubduk.infrastructure.tables.Comments
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class CommentRepositoryLive(env: DatabaseProvider) extends CommentRepository.Service {

  override def getById(commentId: CommentId): IO[ServerError, Option[CommentRecord]] =
    ZIO
      .fromDBIO {
        Comments.table
          .filter(comment => comment.id === commentId)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def getByPostIdPaginated(
    postId: PostId,
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[CommentFilter]
  ): IO[ServerError, Page[CommentRecord]] =
    getByPostId(postId, offset, limit, filters)
      .zipPar(countByPostIdFiltered(postId, filters))
      .map { case (comments, count) => Page(comments, count) }

  override def getByPostId(
    postId: PostId,
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[CommentFilter]
  ): IO[ServerError, Seq[CommentRecord]] =
    ZIO
      .fromDBIO {
        Comments.table
          .filter(_.postId === postId)
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def countByPostIdFiltered(
    postId: PostId,
    filters: BoolAlgebra[CommentFilter]
  ): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Comments.table
          .filter(_.postId === postId)
          .filteredBy(filters)
          .length
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(postId: PostId, comment: CommentRecord): IO[ServerError, CommentId] =
    ZIO
      .fromDBIO {
        Comments.table.returning(Comments.table.map(_.id)) += comment
      }
      .mapError(ServerError)
      .provide(env)

  override def update(commentId: CommentId, contents: String): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Comments.table
          .filter(comment => comment.id === commentId)
          .map(_.contents)
          .update(contents)
      }
      .mapError(ServerError)
      .provide(env)
}
