package io.rubduk

import io.rubduk.domain.repositories.{CommentRepository, MediaRepository, PostRepository, UserRepository}
import io.rubduk.domain.services.{Media, TokenValidation}
import zio.Has

package object domain {
  type UserRepository    = Has[UserRepository.Service]
  type PostRepository    = Has[PostRepository.Service]
  type CommentRepository = Has[CommentRepository.Service]
  type TokenValidation   = Has[TokenValidation.Service]
  type Media             = Has[Media.Service]
  type MediaRepository   = Has[MediaRepository.Service]
}
