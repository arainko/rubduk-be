package io.rubduk

import io.rubduk.domain.repositories.{
  CommentRepository,
  MediaReadRepository,
  MediaRepository,
  PostRepository,
  UserRepository
}
import io.rubduk.domain.services.{MediaApi, TokenValidation}
import zio.Has

package object domain {
  type UserRepository      = Has[UserRepository.Service]
  type PostRepository      = Has[PostRepository.Service]
  type CommentRepository   = Has[CommentRepository.Service]
  type TokenValidation     = Has[TokenValidation.Service]
  type MediaApi            = Has[MediaApi.Service]
  type MediaRepository     = Has[MediaRepository.Service]
  type MediaReadRepository = Has[MediaReadRepository.Service]
}
