package io.rubduk.infrastructure.models

import cats.Functor

final case class Offset(value: Int) extends AnyVal
final case class Limit(value: Int)  extends AnyVal
final case class Page[A](entities: Seq[A], count: RowCount)

object Page {

  /*
  Let's us use a .map method on Page just like it'd be eg. a List.

  What it does is just .map over the entities (which is a Seq) and then copy the Page
  with the mapped entities and return it.
  Link: https://typelevel.org/cats/typeclasses/functor.html
   */
  implicit val pageFunctor: Functor[Page] = new Functor[Page] {

    override def map[A, B](fa: Page[A])(f: A => B): Page[B] =
      fa.copy(entities = fa.entities.map(f))
  }
}
