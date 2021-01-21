package io.rubduk.application

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.{PostRepository, TokenValidation, UserRepository}
import io.rubduk.domain.errors.ApplicationError.{PostAlreadyLiked, PostNotLiked, ServerError, UserNotFound}
import io.rubduk.domain.models.aliases.RowCount
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.post.PostId
import io.rubduk.domain.repositories.LikeRepository
import zio.{Has, ZIO}

object LikeService {

  def likePost(idToken: IdToken, postId: PostId): ZIO[Has[LikeRepository.Service] with PostRepository with UserRepository with TokenValidation, ApplicationError, Unit] =
    for {
      userId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _ <- PostService.getById(postId)
      _ <- LikeRepository.getByPostAndUserId(postId, userId)
        .unrefineTo[ApplicationError]
        .filterOrFail(_.isEmpty)(PostAlreadyLiked)
      _ <- LikeRepository.addLike(postId, userId)
    } yield ()

  def unlikePost(idToken: IdToken, postId: PostId): ZIO[Has[LikeRepository.Service] with PostRepository with UserRepository with TokenValidation, ApplicationError, Unit] =
    for {
      userId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _ <- PostService.getById(postId)
      _ <- LikeRepository.getByPostAndUserId(postId, userId).someOrFail(PostNotLiked)
      _ <- LikeRepository.removeLike(postId, userId)
    } yield ()

  def likeCount(postId: PostId): ZIO[Has[LikeRepository.Service], ServerError, RowCount] =
    LikeRepository.count(postId)

}
