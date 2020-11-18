package io.rubduk.infrastructure.additional

import slick.lifted.{Query, Rep}
import ImprovedPostgresProfile.api._

trait Filter[T] {
  def isApplicable: Boolean
  def apply(table: T): Rep[Boolean]
}

object Filter {

  def optional[T, A](applier: Option[A])(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    OptionFilter(applier, applier.map(a => predicate(_: T, a)).getOrElse(_ => true))

  private case class OptionFilter[T, A](applier: Option[A], predicate: T => Rep[Boolean]) extends Filter[T] {
    override def isApplicable: Boolean         = applier.nonEmpty
    override def apply(table: T): Rep[Boolean] = predicate(table)
  }

  implicit class FilterOps[T, E](val query: Query[T, E, Seq]) extends AnyVal {

    def filteredBy(filters: Seq[Filter[T]]): Query[T, E, Seq] =
      filters.foldLeft(query) { (combinedQuery, filter) =>
        combinedQuery.filterIf(filter.isApplicable)(filter.apply)
      }
  }
}
