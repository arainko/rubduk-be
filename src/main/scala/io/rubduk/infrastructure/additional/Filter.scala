package io.rubduk.infrastructure.additional

import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import shapeless.<:!<
import shapeless.ops.tuple._
import slick.lifted.{Query, Rep}

import scala.annotation.nowarn

trait Filter[T] { self =>
  def isApplicable: Boolean
  def apply(table: T): Rep[Boolean]

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

  @nowarn final def tupled(implicit ev: T <:!< Product): Filter[Tuple1[T]] =
    new Filter[Tuple1[T]] {
      override def isApplicable: Boolean                 = self.isApplicable
      override def apply(table: Tuple1[T]): Rep[Boolean] = self.apply(table._1)
    }

  @nowarn final def ** [A, C](f2: Filter[A])(implicit
    prepend: Prepend.Aux[T, Tuple1[A], C],
    init: Init.Aux[C, T],
    last: Last.Aux[C, A],
    ev: T <:< Product
  ): Filter[C] =
    new Filter[C] {
      override def isApplicable: Boolean         = self.isApplicable || f2.isApplicable
      override def apply(table: C): Rep[Boolean] = self(init(table)) && f2(last(table))
    }

  @nowarn final def ++ [A, C](f2: Filter[A])(implicit
    prepend: Prepend.Aux[T, Tuple1[A], C],
    init: Init.Aux[C, T],
    last: Last.Aux[C, A],
    ev: T <:< Product
  ): Filter[C] =
    new Filter[C] {
      override def isApplicable: Boolean         = self.isApplicable || f2.isApplicable
      override def apply(table: C): Rep[Boolean] = self(init(table)) || f2(last(table))
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

  def sumEmpty[T]: Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean         = false
      override def apply(table: T): Rep[Boolean] = false
    }

  def productEmpty[T]: Filter[T] =
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

  def sequential[T, A](appliers: Seq[A])(applicable: A => Boolean)(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    new Filter[T] {
      override def isApplicable: Boolean = appliers.exists(applicable)

      override def apply(table: T): Rep[Boolean] =
        appliers.foldLeft(false: Rep[Boolean]) { (acc, curr) =>
          acc || predicate(table, curr)
        }
    }

  implicit class FilterOps[T, E](val query: Query[T, E, Seq]) extends AnyVal {

    def filteredBy(filters: Seq[Filter[T]]): Query[T, E, Seq] =
      filters.foldLeft(query) { (combinedQuery, filter) =>
        combinedQuery.filterIf(filter.isApplicable)(filter.apply)
      }
  }
}
