package io.rubduk.domain.services

import java.time.OffsetDateTime

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.CommentError._
import io.rubduk.domain.errors.PostError.PostNotFound
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.repositories.{CommentRepository, PostRepository}
import io.rubduk.domain.{CommentRepository, PostRepository, UserRepository}
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.tables.Comments
import zio.ZIO

object CommentService {

  def getByPostIdPaginated(
    postId: PostId,
    offset: Offset,
    limit: Limit,
    filters: Filter[Comments.Schema]*
  ): ZIO[CommentRepository with PostRepository with UserRepository, ApplicationError, Page[Comment]] =
    for {
      _ <- PostRepository.getById(postId).someOrFail(PostNotFound)
      comments <- CommentRepository.getByPostIdPaginated(postId, offset, limit, filters)
      mergedComments <- ZIO.foreachPar(comments.entities) { comment =>
        UserService.getById(comment.userId).map(comment.toDomain)
      }
    } yield Page(mergedComments, comments.count)

  def getByPostId(
    postId: PostId,
    offset: Offset,
    limit: Limit,
    filters: Filter[Comments.Schema]*
  ): ZIO[CommentRepository with PostRepository with UserRepository, ApplicationError, Seq[Comment]] =
    for {
      _ <- PostRepository.getById(postId).someOrFail(PostNotFound)
      comments <- CommentRepository.getByPostId(postId, offset, limit, filters)
      mergedComments <- ZIO.foreachPar(comments) { comment =>
        UserService.getById(comment.userId).map(comment.toDomain)
      }
    } yield mergedComments


  def getById(postId: PostId, commentId: CommentId): ZIO[CommentRepository with UserRepository, ApplicationError, Comment] =
    CommentRepository
      .getById(commentId)
      .someOrFail(CommentNotFound)
      .filterOrFail(_.postId == postId)(CommentNotUnderPost)
      .flatMap { comment =>
        UserService.getById(comment.userId).map(comment.toDomain)
      }

  def insert(postId: PostId, userId: UserId, comment: CommentDTO): ZIO[CommentRepository, ServerError, CommentId] = {
    val commentToInsert = comment.toDAO(postId, userId, OffsetDateTime.now())
    CommentRepository.insert(postId, commentToInsert)
  }

  def update(
    userId: UserId,
    postId: PostId,
    commentId: CommentId,
    comment: CommentDTO
  ): ZIO[CommentRepository with UserRepository, ApplicationError, Unit] =
    for {
      fetchedComment <- getById(postId, commentId)
      _ <- ZIO.succeed(fetchedComment.user.id)
        .someOrFail(UserNotFound)
        .unrefineTo[ApplicationError]
        .filterOrFail(_ == userId)(CommentNotByThisUser)
      _ <- CommentRepository.update(commentId, comment.contents)
    } yield ()
}
