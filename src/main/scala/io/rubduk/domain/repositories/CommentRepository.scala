package io.rubduk.domain.repositories

import io.rubduk.domain.CommentRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.live.CommentRepositoryLive
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.tables.Comments
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object CommentRepository {

  trait Service {
    def getById(commentId: CommentId): IO[ServerError, Option[CommentDAO]]

    def getByPostIdPaginated(
      postId: PostId,
      offset: Offset,
      limit: Limit,
      filters: Seq[Filter[Comments.Schema]]
    ): IO[ServerError, Page[CommentDAO]]

    def getByPostId(
      postId: PostId,
      offset: Offset,
      limit: Limit,
      filters: Seq[Filter[Comments.Schema]]
    ): IO[ServerError, Seq[CommentDAO]]

    def countByPostIdFiltered(postId: PostId, filters: Seq[Filter[Comments.Schema]]): IO[ServerError, RowCount]

    def insert(postId: PostId, comment: CommentDAO): IO[ServerError, CommentId]

    def update(commentId: CommentId, contents: String): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, CommentRepository] = ZLayer.fromFunction { database =>
    new CommentRepositoryLive(database)
  }
}
