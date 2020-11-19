package io.rubduk.api.serializers

import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.rubduk.infrastructure.models.{Limit, Offset}
import io.rubduk.infrastructure.typeclasses.IdConverter

object unmarshallers {

  val offset: Unmarshaller[String, Offset] =
    Unmarshaller.intFromStringUnmarshaller.map(int => Offset(int))

  val limit: Unmarshaller[String, Limit] =
    Unmarshaller.intFromStringUnmarshaller.map(int => Limit(int))

  def IdParam[A: IdConverter]: Unmarshaller[String, A] =
    Unmarshaller.longFromStringUnmarshaller.map(IdConverter[A].fromLong)
}
