package io.rubduk.config

import zio.config.magnolia.DeriveConfigDescriptor

final case class AuthConfig(clientId: String)

object AuthConfig {
  val descriptor = DeriveConfigDescriptor.descriptor[AuthConfig]
}
