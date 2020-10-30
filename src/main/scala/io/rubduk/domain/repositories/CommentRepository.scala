package io.rubduk.domain.repositories

import io.rubduk.domain.CommentRepository
import io.rubduk.domain.repositories.live.CommentRepositoryLive
import io.rubduk.infrastructure.models._
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{Task, URLayer, ZLayer}

@accessible
object CommentRepository {
  trait Service {
    def getById(commentId: CommentId): Task[Option[CommentDAO]]
    def getAllPaginated(offset: Offset, limit: Limit): Task[Page[CommentDAO]]
    def getAll(offset: Offset, limit: Limit): Task[Seq[CommentDAO]]
    def count: Task[RowCount]
    def insert(comment: CommentDAO): Task[CommentId]
    def update(commentId: CommentId, comment: CommentDAO): Task[RowCount]
  }

  val live: URLayer[DatabaseProvider, CommentRepository] = ZLayer.fromFunction { database =>
    new CommentRepositoryLive(database)
  }
}
