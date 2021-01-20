package io.rubduk

import io.rubduk.application.{MediaApi, TokenValidation}
import io.rubduk.domain.repositories._
import io.rubduk.domain.services.MediaReadService
import zio.Has

package object domain {
  type UserRepository          = Has[UserRepository.Service]
  type PostRepository          = Has[PostRepository.Service]
  type CommentRepository       = Has[CommentRepository.Service]
  type TokenValidation         = Has[TokenValidation.Service]
  type MediaApi                = Has[MediaApi.Service]
  type MediaRepository         = Has[MediaRepository.Service]
  type MediaReadRepository     = Has[MediaReadService.Service]
  type FriendRequestRepository = Has[FriendRequestRepository.Service]
}
