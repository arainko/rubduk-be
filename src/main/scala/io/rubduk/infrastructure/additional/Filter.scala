package io.rubduk.infrastructure.additional

import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import slick.lifted.{Query, Rep}
import cats.syntax.semigroupal._
import cats._
import io.rubduk.infrastructure.tables.{Posts, Users}

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

  final def sum[A](f2: Filter[A]): Filter[(T, A)] =
    new Filter[(T, A)] {
      override def isApplicable: Boolean              = self.isApplicable || f2.isApplicable
      override def apply(table: (T, A)): Rep[Boolean] = self(table._1) || f2(table._2)
    }

  final def sum2[A, B](f1: Filter[A], f2: Filter[B]): Filter[(T, A, B)] =
    new Filter[(T, A, B)] {
      override def isApplicable: Boolean                 = self.isApplicable || f1.isApplicable || f2.isApplicable
      override def apply(table: (T, A, B)): Rep[Boolean] = self(table._1) || f1(table._2) || f2(table._3)
    }

  final def sum3[A, B, C](f1: Filter[A], f2: Filter[B], f3: Filter[C]): Filter[(T, A, B, C)] =
    new Filter[(T, A, B, C)] {
      override def isApplicable: Boolean                 = self.isApplicable || f1.isApplicable || f2.isApplicable || f3.isApplicable
      override def apply(table: (T, A, B, C)): Rep[Boolean] = self(table._1) || f1(table._2) || f2(table._3) || f3(table._4)
    }

  final def product[A](f2: Filter[A]): Filter[(T, A)] =
    new Filter[(T, A)] {
      override def isApplicable: Boolean              = self.isApplicable || f2.isApplicable
      override def apply(table: (T, A)): Rep[Boolean] = self(table._1) && f2(table._2)
    }

  final def product2[A, B](f1: Filter[A], f2: Filter[B]): Filter[(T, A, B)] =
    new Filter[(T, A, B)] {
      override def isApplicable: Boolean                 = self.isApplicable || f1.isApplicable || f2.isApplicable
      override def apply(table: (T, A, B)): Rep[Boolean] = self(table._1) && f1(table._2) && f2(table._3)
    }

  final def product3[A, B, C](f1: Filter[A], f2: Filter[B], f3: Filter[C]): Filter[(T, A, B, C)] =
    new Filter[(T, A, B, C)] {
      override def isApplicable: Boolean                 = self.isApplicable || f1.isApplicable || f2.isApplicable || f3.isApplicable
      override def apply(table: (T, A, B, C)): Rep[Boolean] = self(table._1) && f1(table._2) && f2(table._3) && f3(table._4)
    }
}

object Filter {
  Filter.sumEmpty[Users.Schema]
    .sum(Filter.sumEmpty[Posts.Schema])

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
      override def isApplicable: Boolean = false
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
