package io.rubduk.domain.typeclasses

import shapeless._

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
}
