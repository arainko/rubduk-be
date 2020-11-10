package io.rubduk.domain.services

import java.time.OffsetDateTime

import cats.syntax.functor._
import io.rubduk.domain.CommentRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.CommentError
import io.rubduk.domain.errors.CommentError._
import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import zio.ZIO

object CommentService {
  def getByPostIdPaginated(postId: PostId, offset: Offset, limit: Limit): ZIO[CommentRepository, Nothing, Page[Comment]] =
    CommentRepository.getByPostIdPaginated(postId, offset, limit)
      .orDieWith(ServerError)
      .map(_.map(_.toDomain))

  def getByPostId(postId: PostId, offset: Offset, limit: Limit): ZIO[CommentRepository, Nothing, Seq[Comment]] =
    CommentRepository.getByPostId(postId, offset, limit)
      .orDieWith(ServerError)
      .map(_.map(_.toDomain))

  def getById(postId: PostId, commentId: CommentId): ZIO[CommentRepository, CommentError, Comment] =
    CommentRepository.getById(postId, commentId)
      .flatMap(ZIO.fromOption(_))
      .bimap(_ => CommentNotFound, _.toDomain)

  def insert(postId: PostId, userId: UserId, comment: CommentDTO): ZIO[CommentRepository, Nothing, CommentId] = {
    val commentToInsert = comment.toDomain(postId, userId, OffsetDateTime.now()).toDAO
    CommentRepository.insert(postId, commentToInsert).orDieWith(ServerError)
  }

  def update(commentId: CommentId, comment: CommentDTO): ZIO[CommentRepository, CommentError, Unit] =
    CommentRepository.update(commentId, comment.contents)
      .orDieWith(ServerError)
      .reject { case 0 => CommentNotFound }
      .unit

}
