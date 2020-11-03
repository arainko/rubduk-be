package io.rubduk.domain.services

import java.time.OffsetDateTime

import io.rubduk.domain.errors.ApplicationError.{EntityError, ServerError}
import io.rubduk.domain.errors.PostError.PostNotFound
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.errors.{PostError, UserError}
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.domain.{PostRepository, UserRepository}
import io.rubduk.infrastructure.models._
import zio.ZIO

object PostService {

  def getById(postId: PostId): ZIO[PostRepository with UserRepository, EntityError, Post] =
    for {
      post <- PostRepository.getById(postId)
        .flatMap(ZIO.fromOption(_))
        .orElseFail(PostNotFound)
      user <- UserService.getById(post.userId)
    } yield post.toDomain(user)

  def getAllPaginated(offset: Offset, limit: Limit): ZIO[PostRepository with UserRepository, Throwable, Page[Post]] =
    for {
      posts <- PostRepository.getAllPaginated(offset, limit)
      postsWithUsers <- ZIO.foreachPar(posts.entities) { post =>
        UserService.getById(post.userId).map(user => post.toDomain(user))
      }
    } yield Page(postsWithUsers, posts.count)

  def getAll(offset: Offset, limit: Limit): ZIO[PostRepository with UserRepository, UserError, Seq[Post]] =
    for {
      posts <- PostRepository.getAll(offset, limit).orDieWith(ServerError)
      postsWithUsers <- ZIO.foreachPar(posts) { post =>
        UserService.getById(post.userId).map(user => post.toDomain(user))
      }
    } yield postsWithUsers

  def insert(userId: UserId, post: PostDTO): ZIO[PostRepository with UserRepository, UserError, PostId] =
    for {
      user <- UserService.getById(userId)
      currentDate = OffsetDateTime.now()
      postToInsert = post.toDomain(user, currentDate).toDAO(userId)
      insertedId <- PostRepository.insert(postToInsert).orDieWith(ServerError)
    } yield insertedId

  def update(postId: PostId, userId: UserId, post: PostDTO): ZIO[PostRepository with UserRepository, EntityError, Unit] =
    for {
      postUserId <- PostService.getById(postId).map(_.user.id).someOrFail(UserNotFound)
      _ <- PostRepository.update(postId, post.contents)
        .orDieWith(ServerError)
        .when(postUserId == userId)
    } yield ()

}
