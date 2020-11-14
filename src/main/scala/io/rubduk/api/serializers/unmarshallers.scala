package io.rubduk.api.serializers

import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.rubduk.infrastructure.models.{Limit, Offset}

object unmarshallers {

  val offset: Unmarshaller[String, Offset] =
    Unmarshaller.intFromStringUnmarshaller.map(int => new Offset(int))

  val limit: Unmarshaller[String, Limit] =
    Unmarshaller.intFromStringUnmarshaller.map(int => new Limit(int))
}
