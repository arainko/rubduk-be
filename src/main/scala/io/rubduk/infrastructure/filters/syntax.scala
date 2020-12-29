package io.rubduk.infrastructure.filters

import slick.lifted.Query

object syntax {

  implicit class InterpreterOps[A, B](val filter: A) extends AnyVal {
    def interpret(implicit interpreter: FilterInterpreter[A, B]): B = FilterInterpreter[A, B].apply(filter)
  }

  implicit class FilterOps[T, E](val query: Query[T, E, Seq]) extends AnyVal {

    def filteredBy(filters: Seq[Filter[T]]): Query[T, E, Seq] =
      filters.foldLeft(query) { (combinedQuery, filter) =>
        combinedQuery.filter(filter.apply)
      }
  }
}
