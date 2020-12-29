package io.rubduk.domain.models

import cats.Functor
import io.rubduk.domain.models.aliases.RowCount

object common {
  final case class Offset(value: Int) extends AnyVal
  final case class Limit(value: Int)  extends AnyVal
  final case class Page[A](entities: Seq[A], count: RowCount)

  object Page {

    implicit val pageFunctor: Functor[Page] = new Functor[Page] {

      override def map[A, B](fa: Page[A])(f: A => B): Page[B] =
        fa.copy(entities = fa.entities.map(f))
    }
  }
}
