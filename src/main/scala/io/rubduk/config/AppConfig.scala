package io.rubduk.config

import zio.config.magnolia.DeriveConfigDescriptor
import akka.http.interop.HttpServer
import io.rubduk.config.AppConfig.{AuthConfig, ImgurConfig}



final case class AppConfig(api: HttpServer.Config, auth: AuthConfig, imgur: ImgurConfig)

object AppConfig {
  final case class AuthConfig(clientId: String)
  final case class ImgurConfig(clientId: String, baseApiUrl: String)

  val descriptor = DeriveConfigDescriptor.descriptor[AppConfig]
}
