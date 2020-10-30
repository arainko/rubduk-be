package io.rubduk.domain.filters

import slick.jdbc.PostgresProfile.api._

object Filter {

  implicit class FilterOps[T, E](val query: Query[T, E, Seq]) extends AnyVal {

    def filteredBy(filters: Seq[Filter[T]]): Query[T, E, Seq] = {
      filters.foldLeft(query) { (query, filter) =>
        query.filterIf(filter.shouldBeApplied)(filter.predicate)
      }
    }
  }
}

trait Filter[T] {
  def shouldBeApplied: Boolean
  def predicate(table: T): Rep[Boolean]
}
