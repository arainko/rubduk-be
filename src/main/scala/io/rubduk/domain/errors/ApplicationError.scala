package io.rubduk.domain.errors

sealed trait ApplicationError extends Exception

object ApplicationError {
  sealed trait DomainError extends ApplicationError
  trait EntityError extends DomainError

  final case class ServerError(message: String) extends ApplicationError

  object ServerError extends (Throwable => ServerError) {
    def apply(throwable: Throwable): ServerError =
      ServerError(throwable.getMessage)
  }
}

