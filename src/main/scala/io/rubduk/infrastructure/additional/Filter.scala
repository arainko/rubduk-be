package io.rubduk.infrastructure.additional

import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import slick.lifted.{Query, Rep}

import shapeless._
import ops.tuple.FlatMapper
import syntax.std.tuple._
import test._
import cats.implicits._

trait LowPriorityFlatten extends Poly1 {
  implicit def default[T] = at[T](Tuple1(_))
}
object tFlatten extends LowPriorityFlatten {
  implicit def caseTuple[P <: Product](implicit lfm: Lazy[FlatMapper[P, tFlatten.type]]) =
    at[P](lfm.value(_))
}

trait Filter[A] { self =>
  def isApplicable: Boolean
  def apply(table: A): Rep[Boolean]

  final def combine[B](f2: Filter[B]): Filter[(A, B)] = new Filter[(A, B)] {
    override def isApplicable: Boolean = self.isApplicable && f2.isApplicable
    override def apply(table: (A, B)): Rep[Boolean] = self.apply(table._1) && f2.apply(table._2)
  }

  final def combine(f2: Filter[A]): Filter[A] = new Filter[A] {
    override def isApplicable: Boolean = self.isApplicable && f2.isApplicable
    override def apply(table: A): Rep[Boolean] = self.apply(table) && f2.apply(table)
  }
}

object Filter {

  def flatten[A <: Product, B <: Product](f: Filter[A])(func: B => A): Filter[B] = new Filter[B] {
    override def isApplicable: Boolean = f.isApplicable

    override def apply(table: B): Rep[Boolean] = f.apply(func(table))
  }

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
