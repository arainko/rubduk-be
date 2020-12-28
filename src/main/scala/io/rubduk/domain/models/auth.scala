package io.rubduk.domain.models

import cats.syntax.option._

final case class IdToken(token: String) extends AnyVal

final case class TokenUser(email: String, firstName: String, lastName: String) {
  def toDTO: UserDTO = UserDTO(None, firstName, lastName.some, email, None, None)
}
