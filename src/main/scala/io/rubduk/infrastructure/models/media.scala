package io.rubduk.infrastructure.models


object media {
  final case class Link(value: String) extends AnyVal
  final case class Base64Image(value: String) extends AnyVal
}



