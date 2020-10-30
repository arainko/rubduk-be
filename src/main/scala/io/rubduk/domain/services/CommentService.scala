package io.rubduk.domain.services

import io.rubduk.domain.CommentRepository
import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.infrastructure.models.{Comment, CommentDTO, CommentId, Limit, Offset, Page, PostId}
import cats.syntax.functor._
import Page._
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.CommentError._
import io.rubduk.domain.errors.CommentError
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

  def getById(commentId: CommentId): ZIO[CommentRepository, CommentError, Comment] =
    CommentRepository.getById(commentId)
      .flatMap(ZIO.fromOption(_))
      .bimap(_ => CommentNotFound, _.toDomain)

  def insert = ???
  def update = ???
}
