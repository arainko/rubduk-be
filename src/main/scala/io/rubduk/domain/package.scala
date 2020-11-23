package io.rubduk

import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.repositories.PostRepository
import io.rubduk.domain.repositories.CommentRepository
import io.rubduk.domain.services.TokenValidationService
import zio.Has

package object domain {
  type UserRepository    = Has[UserRepository.Service]
  type PostRepository    = Has[PostRepository.Service]
  type CommentRepository = Has[CommentRepository.Service]
  type TokenValidationService = Has[TokenValidationService.Service]
}
