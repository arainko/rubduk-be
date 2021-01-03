package io.rubduk.infrastructure.filters

import io.rubduk.infrastructure.SlickPGProfile.api._
import shapeless.<:!<
import shapeless.ops.tuple._
import slick.lifted.Rep

import scala.annotation.nowarn

trait Filter[T] { self =>
  def apply(table: T): Rep[Boolean]

  final def && (f2: Filter[T]): Filter[T] =
    (table: T) => self(table) && f2(table)

  final def || (f2: Filter[T]): Filter[T] =
    (table: T) => self(table) || f2(table)

  final def unary_! : Filter[T] =
    (table: T) => !self(table)

  @nowarn final def joinable(implicit ev: T <:!< Product): Filter[Tuple1[T]] =
    (table: Tuple1[T]) => self(table._1)

  @nowarn final def ** [A, C](f2: Filter[A])(implicit
    prepend: Prepend.Aux[T, Tuple1[A], C],
    init: Init.Aux[C, T],
    last: Last.Aux[C, A],
    ev: T <:< Product
  ): Filter[C] =
    (table: C) => self(init(table)) && f2(last(table))

  @nowarn final def ++ [A, C](f2: Filter[A])(implicit
    prepend: Prepend.Aux[T, Tuple1[A], C],
    init: Init.Aux[C, T],
    last: Last.Aux[C, A],
    ev: T <:< Product
  ): Filter[C] =
    (table: C) => self(init(table)) || f2(last(table))
}

object Filter {

  def apply[T](predicate: T => Rep[Boolean]): Filter[T] = predicate(_)

  def sumEmpty[T]: Filter[T] = (_: T) => false

  def productEmpty[T]: Filter[T] = (_: T) => true

  def sequentialSum[T, A](appliers: Seq[A])(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    (table: T) =>
      appliers.foldLeft(false: Rep[Boolean]) { (acc, curr) =>
        acc || predicate(table, curr)
      }

  def sequentialProduct[T, A](appliers: Seq[A])(predicate: (T, A) => Rep[Boolean]): Filter[T] =
    (table: T) =>
      appliers.foldLeft(true: Rep[Boolean]) { (acc, curr) =>
        acc && predicate(table, curr)
      }
}
