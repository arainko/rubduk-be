package io.rubduk.domain.errors

import io.rubduk.domain.errors.ApplicationError.EntityError

sealed trait CommentError extends EntityError

object CommentError {
  case object CommentNotFound extends CommentError
}
