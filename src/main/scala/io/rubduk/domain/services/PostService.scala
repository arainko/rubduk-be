package io.rubduk.domain.services

import cats.syntax.functor._
import io.rubduk.domain.{PostRepository, UserRepository}
import io.rubduk.domain.errors.ApplicationError.{EntityError, ServerError}
import io.rubduk.domain.errors.{PostError, UserError}
import io.rubduk.domain.errors.PostError.PostNotFound
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import zio.{Has, ZIO}

object PostService {

  def getById(postId: PostId): ZIO[PostRepository with UserRepository, EntityError, Post] =
    for {
      post <- PostRepository.getById(postId)
        .flatMap(ZIO.fromOption(_))
        .orElseFail(PostNotFound)
      user <- UserService.getById(post.userId)
    } yield post.toDomain(user)

  def getAllPaginated(offset: Offset, limit: Limit): ZIO[PostRepository with UserRepository, UserError, Page[Post]] =
    for {
      posts <- PostRepository.getAllPaginated(offset, limit).orDieWith(ServerError)
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

  def insert(post: PostDTO): ZIO[PostRepository with UserRepository, UserError, PostId] =
    for {
      userId <- UserService.getByEmail(post.user.email)
        .map(_.id)
        .flatMap(ZIO.fromOption(_))
        .orElseFail(UserNotFound)
      insertedId <- PostRepository.insert(post.toDomain.toDAO(userId)).orDieWith(ServerError)
    } yield insertedId

  def update(postId: PostId, post: PostDTO): ZIO[PostRepository, PostError, Unit] =
    PostRepository.update(postId, post.contents)
      .orDieWith(ServerError)
      .reject { case 0 => PostNotFound }
      .unit

}
