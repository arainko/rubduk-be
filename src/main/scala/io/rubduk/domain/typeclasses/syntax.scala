package io.rubduk.domain.typeclasses

import BoolAlgebra._
import shapeless.<:!<

import scala.annotation.nowarn

object syntax {

  implicit class BoolAlgebraOps[A](val value: A) extends AnyVal {
    def lift(implicit @nowarn ev: A <:!< BoolAlgebra[_]): BoolAlgebra[A] = Pure(value)
  }
}
