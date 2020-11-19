package io.rubduk.domain

import cats.implicits.catsSyntaxOptionId
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.UserId
import io.rubduk.infrastructure.tables.{Comments, Posts}
import io.rubduk.infrastructure.typeclasses.IdConverter._

import shapeless._
import ops.tuple.FlatMapper
import syntax.std.tuple._
import test._

object mapper extends Poly1 {
  implicit def atSingle[A] = at[Tuple1[A]](identity)
  implicit def atTuple[A, B] = at[(A, B)](a => Tuple2(a_.1, a._2))
}

object filters {

  def postUserIdFilter(userId: Option[UserId]): Filter[Posts.Schema] =
    Filter.optional(userId)(_.userId === _)

  def commentUserIdFilter(userId: Option[UserId]): Filter[Comments.Schema] =
    Filter.optional(userId)(_.userId === _)

  def costam: Filter[((Comments.Schema, Posts.Schema), Posts.Schema)] =

      commentUserIdFilter(UserId(1).some)
      .combine(commentUserIdFilter(UserId(1).some))
      .combine(commentUserIdFilter(UserId(1).some))
        .combine(postUserIdFilter(UserId(1).some))
        .combine(postUserIdFilter(UserId(1).some))

  def de: Filter[(Comments.Schema, Posts.Schema, Posts.Schema)] = Filter.flatten(costam) { a =>
    a.productElements.collect()
  }

}
