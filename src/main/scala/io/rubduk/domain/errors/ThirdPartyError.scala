package io.rubduk.domain.errors

final case class ThirdPartyError(message: String) extends ApplicationError {
  override def getMessage: String = message
}

object ThirdPartyError extends (Throwable => ThirdPartyError) {
  override def apply(v1: Throwable): ThirdPartyError = new ThirdPartyError(v1.getMessage)
}
