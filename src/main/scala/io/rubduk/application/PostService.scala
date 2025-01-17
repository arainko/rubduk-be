package io.rubduk.application

import cats.syntax.option._
import io.rubduk.domain._
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.common.{Limit, Offset, Page}
import io.rubduk.domain.models.post.{Post, PostDTO, PostFilter, PostId}
import io.rubduk.domain.models.user.UserId
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.domain.typeclasses.BoolAlgebra
import zio.ZIO

import java.time.OffsetDateTime

object PostService {

  def getById(
    postId: PostId
  ): ZIO[PostRepository with UserRepository with LikeRepository, ApplicationError, Post] =
    for {
      post  <- PostRepository.getById(postId).someOrFail(PostNotFound)
      user  <- UserService.getById(post.userId)
      likes <- LikeService.likeCount(postId)
    } yield post.toDomain(user, likes)

  def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[PostFilter]
  ): ZIO[PostRepository with UserRepository with LikeRepository, ApplicationError, Page[Post]] =
    for {
      posts <- PostRepository.getAllPaginated(offset, limit, filters)
      postsWithUsers <- ZIO.foreachPar(posts.entities) { post =>
        for {
          user  <- UserService.getById(post.userId)
          likes <- LikeService.likeCount(post.id.get)
        } yield post.toDomain(user, likes)
      }
    } yield Page(postsWithUsers, posts.count)

  def getAll(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[PostFilter]
  ): ZIO[PostRepository with UserRepository with LikeRepository, ApplicationError, Seq[Post]] =
    for {
      posts <- PostRepository.getAll(offset, limit, filters)
      postsWithUsers <- ZIO.foreachPar(posts) { post =>
        for {
          user  <- UserService.getById(post.userId)
          likes <- LikeService.likeCount(post.id.get)
        } yield post.toDomain(user, likes)
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
  ): ZIO[PostRepository with UserRepository with LikeRepository, ApplicationError, Unit] =
    PostService
      .getById(postId)
      .filterOrFail(_.user.id == userId.some)(PostNotByThisUser)
      .zipRight(PostRepository.update(postId, post.contents))
      .unit

  def delete(
    idToken: IdToken,
    postId: PostId
  ): ZIO[PostRepository with UserRepository with LikeRepository with TokenValidation, ApplicationError, Unit] =
    for {
      userId     <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      postUserId <- PostService.getById(postId).map(_.user.id).someOrFail(PostNotFound)
      _          <- ZIO.cond(postUserId == userId, (), PostNotByThisUser)
      _          <- PostRepository.delete(postId)
    } yield ()
}
