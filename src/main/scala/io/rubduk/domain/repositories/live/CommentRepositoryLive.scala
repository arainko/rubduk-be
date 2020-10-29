package io.rubduk.domain.repositories.live

import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.infrastructure.models.{Limit, Offset, Page, RowCount, CommentDAO, CommentId}
import io.rubduk.infrastructure.tables.Comments
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import io.rubduk.infrastructure.converters.IdConverter._
import slick.jdbc.PostgresProfile.api._
import zio.{Task, ZIO}

class CommentRepositoryLive(env: DatabaseProvider) extends CommentRepository.Service {
  override def getById(commentId: CommentId): Task[Option[CommentDAO]] =
    ZIO.fromDBIO {
      Comments.table
        .filter(_.id === commentId)
        .result
        .headOption
    }.provide(env)

  override def getAllPaginated(offset: Offset, limit: Limit): Task[Page[CommentDAO]] =
    getAll(offset, limit).zipPar(count).map {
      case (comments, commentCount) => Page(comments, commentCount)
    }

  override def getAll(offset: Offset, limit: Limit): Task[Seq[CommentDAO]] =
    ZIO.fromDBIO {
      Comments.table
        .drop(offset.value)
        .take(limit.value)
        .result
    }.provide(env)

  override def count: Task[RowCount] =
    ZIO.fromDBIO {
      Comments.table.length.result
    }.provide(env)

  override def insert(comment: CommentDAO): Task[CommentId] =
    ZIO.fromDBIO {
      Comments.table.returning(Comments.table.map(_.id)) += comment
    }.provide(env)

  override def update(commentId: CommentId, comment: CommentDAO): Task[RowCount] =
    ZIO.fromDBIO {
      Comments.table
        .map(c => (c.contents))
        .update((comment.contents))
    }.provide(env)
}
