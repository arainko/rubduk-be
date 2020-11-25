package io.rubduk.config

import zio.config.magnolia.DeriveConfigDescriptor
import akka.http.interop.HttpServer

final case class AppConfig(api: HttpServer.Config, auth: AuthConfig)

object AppConfig {
  val descriptor = DeriveConfigDescriptor.descriptor[AppConfig]
}
