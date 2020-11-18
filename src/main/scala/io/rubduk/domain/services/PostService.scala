package io.rubduk.domain.services

import java.time.OffsetDateTime

import cats.implicits.catsSyntaxOptionId
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.PostError.{PostNotByThisUser, PostNotFound}
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.domain.{PostRepository, UserRepository}
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.tables.Posts
import zio.ZIO

object PostService {

  def getById(postId: PostId): ZIO[PostRepository with UserRepository, ApplicationError, Post] =
    for {
      post <- PostRepository.getById(postId).someOrFail(PostNotFound)
      user <- UserService.getById(post.userId)
    } yield post.toDomain(user)

  def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: Filter[Posts.Schema]*
  ): ZIO[PostRepository with UserRepository, ApplicationError, Page[Post]] =
    for {
      posts <- PostRepository.getAllPaginated(offset, limit, filters)
      postsWithUsers <- ZIO.foreachPar(posts.entities) { post =>
        UserService.getById(post.userId).map(user => post.toDomain(user))
      }
    } yield Page(postsWithUsers, posts.count)

  def getAll(
    offset: Offset,
    limit: Limit,
    filters: Filter[Posts.Schema]*
  ): ZIO[PostRepository with UserRepository, ApplicationError, Seq[Post]] =
    for {
      posts <- PostRepository.getAll(offset, limit, filters)
      postsWithUsers <- ZIO.foreachPar(posts) { post =>
        UserService.getById(post.userId).map(user => post.toDomain(user))
      }
    } yield postsWithUsers

  def insert(userId: UserId, post: PostDTO): ZIO[PostRepository with UserRepository, ApplicationError, PostId] =
    for {
      user <- UserService.getById(userId)
      currentDate  = OffsetDateTime.now()
      postToInsert = post.toDomain(user, currentDate).toDAO(userId)
      insertedId <- PostRepository.insert(postToInsert)
    } yield insertedId

  def update(
    postId: PostId,
    userId: UserId,
    post: PostDTO
  ): ZIO[PostRepository with UserRepository, ApplicationError, Unit] =
    PostService
      .getById(postId)
      .filterOrFail(_.user.id == userId.some)(PostNotByThisUser)
      .zipRight(PostRepository.update(postId, post.contents))
      .unit
}
