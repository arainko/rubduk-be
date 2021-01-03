package io.rubduk.domain.repositories

import io.rubduk.domain.CommentRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.comment._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.post._
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.repositories.CommentRepositoryLive
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object CommentRepository {

  trait Service {
    def getById(commentId: CommentId): IO[ServerError, Option[CommentRecord]]

    def getByPostIdPaginated(
      postId: PostId,
      offset: Offset,
      limit: Limit,
      filters: BoolAlgebra[CommentFilter]
    ): IO[ServerError, Page[CommentRecord]]

    def getByPostId(
      postId: PostId,
      offset: Offset,
      limit: Limit,
      filters: BoolAlgebra[CommentFilter]
    ): IO[ServerError, Seq[CommentRecord]]

    def countByPostIdFiltered(postId: PostId, filters: BoolAlgebra[CommentFilter]): IO[ServerError, RowCount]

    def insert(postId: PostId, comment: CommentRecord): IO[ServerError, CommentId]

    def update(commentId: CommentId, contents: String): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, CommentRepository] = ZLayer.fromFunction { database =>
    new CommentRepositoryLive(database)
  }
}
