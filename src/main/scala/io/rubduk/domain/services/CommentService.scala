package io.rubduk.domain.services

import java.time.OffsetDateTime

import cats.syntax.functor._
import io.rubduk.domain.CommentRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.{ApplicationError, CommentError}
import io.rubduk.domain.errors.CommentError._
import io.rubduk.domain.errors.PostError.PostNotFound
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import zio.ZIO

object CommentService {

  def getByPostIdPaginated(
    postId: PostId,
    offset: Offset,
    limit: Limit
  ): ZIO[CommentRepository, Throwable, Page[Comment]] =
    CommentRepository
      .getByPostIdPaginated(postId, offset, limit)
      .map(_.map(_.toDomain))

  def getByPostId(postId: PostId, offset: Offset, limit: Limit): ZIO[CommentRepository, Throwable, Seq[Comment]] =
    CommentRepository
      .getByPostId(postId, offset, limit)
      .map(_.map(_.toDomain))

  def getById(postId: PostId, commentId: CommentId): ZIO[CommentRepository, Throwable, Comment] =
    CommentRepository
      .getById(commentId)
      .someOrFail(CommentNotFound)
      .filterOrFail(_.postId == postId)(CommentNotUnderPost)
      .map(_.toDomain)

  def insert(postId: PostId, userId: UserId, comment: CommentDTO): ZIO[CommentRepository, Nothing, CommentId] = {
    val commentToInsert = comment.toDomain(postId, userId, OffsetDateTime.now()).toDAO
    CommentRepository.insert(postId, commentToInsert).orDieWith(ServerError)
  }

  def update(
    userId: UserId,
    postId: PostId,
    commentId: CommentId,
    comment: CommentDTO
  ): ZIO[CommentRepository, Throwable, Unit] =
      CommentRepository.getById(commentId)
        .someOrFail(CommentNotFound)
        .filterOrFail(_.userId == userId)(CommentNotByThisUser)
        .filterOrFail(_.postId == postId)(CommentNotUnderPost)
        .zipRight(CommentRepository.update(commentId, comment.contents))
        .unit
}
