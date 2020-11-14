package io.rubduk.domain.repositories.live

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.typeclasses.IdConverter._
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.tables.Comments
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class CommentRepositoryLive(env: DatabaseProvider) extends CommentRepository.Service {

  override def getById(commentId: CommentId): IO[ServerError, Option[CommentDAO]] =
    ZIO
      .fromDBIO {
        Comments.table
          .filter(comment => comment.id === commentId)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def getByPostIdPaginated(postId: PostId, offset: Offset, limit: Limit): IO[ServerError, Page[CommentDAO]] =
    getByPostId(postId, offset, limit)
      .zipPar(countByPostId(postId))
      .map { case (comments, count) => Page(comments, count) }

  override def getByPostId(postId: PostId, offset: Offset, limit: Limit): IO[ServerError, Seq[CommentDAO]] =
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

  override def countByPostId(postId: PostId): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Comments.table
          .filter(_.postId === postId)
          .length
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(postId: PostId, comment: CommentDAO): IO[ServerError, CommentId] =
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
