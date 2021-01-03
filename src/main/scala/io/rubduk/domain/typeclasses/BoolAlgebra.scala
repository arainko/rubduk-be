package io.rubduk.domain.typeclasses

import BoolAlgebra._
import cats.Functor

sealed trait BoolAlgebra[+A] {
  final def &&& [B >: A](that: BoolAlgebra[B]): BoolAlgebra[B] = And(this, that)
  final def ||| [B >: A](that: BoolAlgebra[B]): BoolAlgebra[B] = Or(this, that)
  final def unary_! : BoolAlgebra[A]                           = Not(this)
}

object BoolAlgebra {
  implicit val algebraFunctor: Functor[BoolAlgebra] = new Functor[BoolAlgebra] {

    override def map[A, B](fa: BoolAlgebra[A])(f: A => B): BoolAlgebra[B] =
      fa match {
        case Pure(value)      => Pure(f(value))
        case And(left, right) => And(map(left)(f), map(right)(f))
        case Or(left, right)  => Or(map(left)(f), map(right)(f))
        case Not(value)       => Not(map(value)(f))
        case True             => True
        case False            => False
      }
  }

  final case class Pure[+A](value: A)                                   extends BoolAlgebra[A]
  final case class And[+A](left: BoolAlgebra[A], right: BoolAlgebra[A]) extends BoolAlgebra[A]
  final case class Or[+A](left: BoolAlgebra[A], right: BoolAlgebra[A])  extends BoolAlgebra[A]
  final case class Not[+A](value: BoolAlgebra[A])                       extends BoolAlgebra[A]

  final case object True extends BoolAlgebra[Nothing] {
    def apply[A]: BoolAlgebra[A] = True
  }

  final case object False extends BoolAlgebra[Nothing] {
    def apply[A]: BoolAlgebra[A] = False
  }
}
