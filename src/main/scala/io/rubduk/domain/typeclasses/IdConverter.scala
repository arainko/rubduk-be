package io.rubduk.domain.typeclasses

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import io.rubduk.infrastructure.SlickPGProfile.api._
import shapeless.{::, Generic, HNil}

import scala.reflect.ClassTag

trait IdConverter[A] {
  def fromLong(value: Long): A
  def toLong(id: A): Long
}

object IdConverter {

  def apply[A: IdConverter]: IdConverter[A] = implicitly

  implicit def derive[A <: AnyVal](implicit
    gen: Generic.Aux[A, Long :: HNil]
  ): IdConverter[A] =
    new IdConverter[A] {
      override def fromLong(value: Long): A = gen.from(value :: HNil)
      override def toLong(value: A): Long   = gen.to(value).head
    }

  implicit def idMapper[A: IdConverter: ClassTag]: BaseColumnType[A] =
    MappedColumnType.base[A, Long](
      IdConverter[A].toLong,
      IdConverter[A].fromLong
    )

  def Id[A: IdConverter]: PathMatcher1[A] = LongNumber.map(IdConverter[A].fromLong)
}
