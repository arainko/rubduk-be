package io.rubduk.domain.repositories

import io.rubduk.domain.PostRepository
import io.rubduk.domain.repositories.live.PostRepositoryLive
import io.rubduk.infrastructure.models._
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{Task, URLayer, ZLayer}

@accessible
object PostRepository {
  trait Service {
    def getById(postId: PostId): Task[Option[PostDAO]]
    def getAllPaginated(offset: Offset, limit: Limit): Task[Page[PostDAO]]
    def getAll(offset: Offset, limit: Limit): Task[Seq[PostDAO]]
    def count: Task[RowCount]
    def insert(post: PostDAO): Task[PostId]
    def update(postId: PostId, post: PostDAO): Task[RowCount]
  }

  val live: URLayer[DatabaseProvider, PostRepository] = ZLayer.fromFunction { database =>
    new PostRepositoryLive(database)
  }
}
