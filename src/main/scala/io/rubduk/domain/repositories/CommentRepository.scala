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
    def getById(postId: PostId, commentId: CommentId): Task[Option[CommentDAO]]
    def getByPostIdPaginated(postId: PostId, offset: Offset, limit: Limit): Task[Page[CommentDAO]]
    def getByPostId(postId: PostId, offset: Offset, limit: Limit): Task[Seq[CommentDAO]]
    def countByPostId(postId: PostId): Task[RowCount]
    def insert(postId: PostId, comment: CommentDAO): Task[CommentId]
    def update(userId: UserId, postId: PostId, commentId: CommentId, contents: String): Task[RowCount]
  }

  val live: URLayer[DatabaseProvider, CommentRepository] = ZLayer.fromFunction { database =>
    new CommentRepositoryLive(database)
  }
}
