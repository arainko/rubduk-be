package io.rubduk.api.serializers

import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.rubduk.domain.typeclasses.IdConverter
import io.rubduk.domain.models.common._

object unmarshallers {

  val offset: Unmarshaller[String, Offset] =
    Unmarshaller.intFromStringUnmarshaller.map(int => Offset(int))

  val limit: Unmarshaller[String, Limit] =
    Unmarshaller.intFromStringUnmarshaller.map(int => Limit(int))

  def IdParam[A: IdConverter]: Unmarshaller[String, A] =
    Unmarshaller.longFromStringUnmarshaller.map(IdConverter[A].fromLong)
}
