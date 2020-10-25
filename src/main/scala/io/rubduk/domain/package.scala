package io.rubduk

import io.rubduk.domain.repositories.UserRepository
import zio.Has

package object domain {
  type UserRepository = Has[UserRepository.Service]
}
