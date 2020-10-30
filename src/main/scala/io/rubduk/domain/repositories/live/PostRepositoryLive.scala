package io.rubduk.domain.repositories.live

import io.rubduk.domain.repositories.PostRepository
import io.rubduk.infrastructure.models.{Limit, Offset, Page, PostDAO, PostId, RowCount, UserId}
import io.rubduk.infrastructure.tables.Posts
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import io.rubduk.infrastructure.converters.IdConverter._
import slick.jdbc.PostgresProfile.api._
import zio.{IO, Task, ZIO}

class PostRepositoryLive(env: DatabaseProvider) extends PostRepository.Service {
  
  override def getById(postId: PostId): Task[Option[PostDAO]] =
    ZIO.fromDBIO {
      Posts.table
        .filter(_.id === postId)
        .result
        .headOption
    }.provide(env)
  
  override def getAllPaginated(offset: Offset, limit: Limit): Task[Page[PostDAO]] =
    getAll(offset, limit).zipPar(count).map {
      case (posts, postCount) => Page(posts, postCount)
    }

  override def getAll(offset: Offset, limit: Limit): Task[Seq[PostDAO]] =
    ZIO.fromDBIO {
      Posts.table
        .drop(offset.value)
        .take(limit.value)
        .result
    }.provide(env)

  override def count: Task[RowCount] =
    ZIO.fromDBIO {
      Posts.table.length.result
    }.provide(env)

  override def insert(post: PostDAO): Task[PostId] =
    ZIO.fromDBIO {
      Posts.table.returning(Posts.table.map(_.id)) += post
    }.provide(env)

  override def update(postId: PostId, contents: String): Task[RowCount] =
    ZIO.fromDBIO {
      Posts.table
        .map(p => p.contents)
        .update(contents)
    }.provide(env)
}
