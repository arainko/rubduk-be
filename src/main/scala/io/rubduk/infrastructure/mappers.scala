package io.rubduk.infrastructure

import io.rubduk.domain.typeclasses.IdConverter
import SlickPGProfile.api._

import scala.reflect.ClassTag

object mappers {

  implicit def idMapper[A: IdConverter: ClassTag]: BaseColumnType[A] =
    MappedColumnType.base[A, Long](
      IdConverter[A].toLong,
      IdConverter[A].fromLong
    )

}
