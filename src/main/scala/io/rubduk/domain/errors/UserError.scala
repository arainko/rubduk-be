package io.rubduk.domain.errors

import io.rubduk.domain.errors.ApplicationError.EntityError

sealed trait UserError extends EntityError

object UserError {
  case object UserNotFound      extends UserError
  case object UserAlreadyExists extends UserError
}
