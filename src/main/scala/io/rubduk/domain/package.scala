package io.rubduk

import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.repositories.PostRepository
import zio.Has

package object domain {
  type UserRepository = Has[UserRepository.Service]
  type PostRepository = Has[PostRepository.Service]
}
