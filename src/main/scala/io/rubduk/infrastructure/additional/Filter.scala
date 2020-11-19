package io.rubduk.infrastructure.additional

import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import slick.lifted.{Query, Rep}

trait Filter[T] { self =>
  def isApplicable: Boolean
  def apply(table: T): Rep[Boolean]

  final def join[A](f2: Filter[A]): Filter[(T, A)] =
    new Filter[(T, A)] {
      override def isApplicable: Boolean              = self.isApplicable || f2.isApplicable
      override def apply(table: (T, A)): Rep[Boolean] = self.apply(table._1) && f2.apply(table._2)
    }

  final def && (f2: Filter[T]): Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean         = self.isApplicable || f2.isApplicable
      override def apply(table: T): Rep[Boolean] = self.apply(table) && f2.apply(table)
    }

  final def || (f2: Filter[T]): Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean         = self.isApplicable || f2.isApplicable
      override def apply(table: T): Rep[Boolean] = self.apply(table) || f2.apply(table)
    }

  final def rearrange[A](f: A => T): Filter[A] =
    new Filter[A] {
      override def isApplicable: Boolean         = self.isApplicable
      override def apply(table: A): Rep[Boolean] = self.apply(f(table))
    }
}

object Filter {

  private[this] case class OptionFilter[T, A](
    applier: Option[A],
    applicable: A => Boolean,
    predicate: T => Rep[Boolean]
  ) extends Filter[T] {
    override def isApplicable: Boolean         = applier.exists(applicable)
    override def apply(table: T): Rep[Boolean] = predicate(table)
  }

  def unit[T]: Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean         = false
      override def apply(table: T): Rep[Boolean] = true
    }

  def applicable[T, A](applier: A)(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean         = true
      override def apply(table: T): Rep[Boolean] = predicate(table, applier)
    }

  def optional[T, A](applier: Option[A])(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    OptionFilter(
      applier,
      (_: A) => true,
      applier.map(a => predicate(_: T, a)).getOrElse(_ => true)
    )

  def optionalValidated[T, A](
    applier: Option[A]
  )(applicable: A => Boolean)(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    OptionFilter(
      applier,
      applicable,
      applier.map(a => predicate(_: T, a)).getOrElse(_ => true)
    )

  def sequence[T, A](appliers: Seq[A])(applicable: A => Boolean)(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean = appliers.exists(applicable)

      override def apply(table: T): Rep[Boolean] =
        appliers.foldLeft(false: Rep[Boolean]) { (acc, curr) =>
          acc || predicate(table, curr)
        }
    }

  implicit class FilterOps[T <: Table[_], E](val query: Query[T, E, Seq]) extends AnyVal {

    def filteredBy(filters: Seq[Filter[T]]): Query[T, E, Seq] =
      filters.foldLeft(query) { (combinedQuery, filter) =>
        combinedQuery.filterIf(filter.isApplicable)(filter.apply)
      }
  }
}
