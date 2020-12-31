package io.rubduk.infrastructure.filters

import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.filters.FilterInterpreter.SlickInterpreter
import slick.lifted.Query
import cats.syntax.functor._
import BoolAlgebra._

object syntax {

  implicit class InterpreterOps[A, B](val filter: A) extends AnyVal {
    def interpret(implicit interpreter: FilterInterpreter[A, B]): B = FilterInterpreter[A, B].apply(filter)
  }

  implicit class FilterOps[T, E](val query: Query[T, E, Seq]) extends AnyVal {
    
    private def interpretAlgebra[A](algebra: BoolAlgebra[Filter[A]]): Filter[A] =
      algebra match {
        case BoolAlgebra.Pure(value)      => value
        case BoolAlgebra.And(left, right) => interpretAlgebra(left) && interpretAlgebra(right)
        case BoolAlgebra.Or(left, right)  => interpretAlgebra(left) || interpretAlgebra(right)
        case BoolAlgebra.Not(value)       => !interpretAlgebra(value)
      }

    def filteredBy[A](filter: BoolAlgebra[A])(implicit interpreter: SlickInterpreter[A, T]): Query[T, E, Seq] =
      query.filter(interpretAlgebra(filter.map(interpreter.apply)).apply)

    def filteredBy(filter: Filter[T]): Query[T, E, Seq] =
      query.filter(filter.apply)
  }
}
