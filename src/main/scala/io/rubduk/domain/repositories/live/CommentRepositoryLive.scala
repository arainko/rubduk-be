package io.rubduk.domain.repositories.live

import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.infrastructure.models.{CommentDAO, CommentId, Limit, Offset, Page, PostId, RowCount}
import io.rubduk.infrastructure.tables.Comments
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import io.rubduk.infrastructure.converters.IdConverter._
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import zio.{Task, ZIO}

class CommentRepositoryLive(env: DatabaseProvider) extends CommentRepository.Service {
  override def getById(postId: PostId, commentId: CommentId): Task[Option[CommentDAO]] =
    ZIO.fromDBIO {
      Comments.table
        .filter {
          comment => comment.id === commentId && comment.postId === postId
        }.result
        .headOption
    }.provide(env)

  override def getByPostIdPaginated(postId: PostId, offset: Offset, limit: Limit): Task[Page[CommentDAO]] =
    getByPostId(postId, offset, limit)
      .zipPar(countByPostId(postId))
      .map { case (comments, count) => Page(comments, count) }

  override def getByPostId(postId: PostId, offset: Offset, limit: Limit): Task[Seq[CommentDAO]] =
    ZIO.fromDBIO {
      Comments.table
        .filter(_.postId === postId)
        .drop(offset.value)
        .take(limit.value)
        .result
    }.provide(env)

  override def countByPostId(postId: PostId): Task[RowCount] =
    ZIO.fromDBIO {
      Comments.table
        .filter(_.postId === postId)
        .length
        .result
    }.provide(env)

  override def insert(postId: PostId, comment: CommentDAO): Task[CommentId] =
    ZIO.fromDBIO {
      Comments.table.returning(Comments.table.map(_.id)) += comment
    }.provide(env)

  override def update(commentId: CommentId, contents: String): Task[RowCount] =
    ZIO.fromDBIO {
      Comments.table
        .map(_.contents)
        .update(contents)
    }.provide(env)
}
