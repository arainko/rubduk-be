package io.rubduk.domain.errors

final case class ValidationError(message: String) extends ApplicationError
