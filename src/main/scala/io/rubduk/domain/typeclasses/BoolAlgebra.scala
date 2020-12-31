package io.rubduk.domain.typeclasses

import BoolAlgebra._
import cats.Functor

sealed trait BoolAlgebra[A] {
  final def && (that: BoolAlgebra[A]): And[A] = And(this, that)
  final def || (that: BoolAlgebra[A]): Or[A]  = Or(this, that)
  final def unary_! : Not[A]                  = Not(this)
}

object BoolAlgebra {

  trait Interpreter[A] {
    def apply(algebra: BoolAlgebra[A]): A
  }

  implicit val algebraFunctor: Functor[BoolAlgebra] = new Functor[BoolAlgebra] {

    override def map[A, B](fa: BoolAlgebra[A])(f: A => B): BoolAlgebra[B] =
      fa match {
        case Pure(value)      => Pure(f(value))
        case And(left, right) => And(map(left)(f), map(right)(f))
        case Or(left, right)  => Or(map(left)(f), map(right)(f))
        case Not(value)       => Not(map(value)(f))
      }
  }

  final case class Pure[A](value: A)                                   extends BoolAlgebra[A]
  final case class And[A](left: BoolAlgebra[A], right: BoolAlgebra[A]) extends BoolAlgebra[A]
  final case class Or[A](left: BoolAlgebra[A], right: BoolAlgebra[A])  extends BoolAlgebra[A]
  final case class Not[A](value: BoolAlgebra[A])                       extends BoolAlgebra[A]
}
