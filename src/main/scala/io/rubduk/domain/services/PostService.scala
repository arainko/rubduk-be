package io.rubduk.domain.services

import cats.syntax.functor._
import io.rubduk.domain.PostRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.PostError
import io.rubduk.domain.errors.PostError.PostNotFound
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import zio.ZIO

package object PostService {

  def getById(postId: PostId): ZIO[PostRepository, PostError, Post] =
    ZIO.accessM[PostRepository](_.get.getById(postId))
      .flatMap(ZIO.fromOption(_))
      .bimap(_ => PostNotFound, _.toDomain(???, ???))

  def getAllPaginated(offset: Offset, limit: Limit): ZIO[PostRepository, Nothing, Page[Post]] =
    ZIO.accessM[PostRepository](_.get.getAllPaginated(offset, limit))
      .orDieWith(ServerError)
      .map(_.map(_.toDomain(???, ???)))

  def getAll(offset: Offset, limit: Limit): ZIO[PostRepository, Nothing, Seq[Post]] =
    ZIO.accessM[PostRepository](_.get.getAll(offset, limit))
      .orDieWith(ServerError)
      .map(_.map(_.toDomain(???, ???)))

  def insert(post: PostDTO): ZIO[PostRepository, Nothing, PostId] =
    ZIO.accessM[PostRepository](_.get.insert(post.toDAO(???)))
      .orDieWith(ServerError)

  def update(postId: PostId, post: PostDTO): ZIO[PostRepository, PostError, Unit] =
    ZIO.accessM[PostRepository](_.get.update(postId, post.toDAO(???)))
      .reject { case 0 => PostNotFound }
      .refineToOrDie[PostError]
      .unit
}
