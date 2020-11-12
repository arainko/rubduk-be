package io.rubduk.domain.errors

import io.rubduk.domain.errors.ApplicationError.EntityError

sealed trait PostError extends EntityError

object PostError {
  case object PostNotFound      extends PostError
  case object PostNotByThisUser extends PostError
}
