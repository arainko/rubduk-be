package io.rubduk.domain

import io.rubduk.domain.filters.postUserIdFilter
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.{PostDAO, UserDAO, UserId}
import io.rubduk.infrastructure.tables.{Posts, Users}
import io.rubduk.infrastructure.typeclasses.IdConverter._
import io.rubduk.infrastructure.additional.Filter.FilterOps

object filters {

  def postUserIdFilter(userId: Option[UserId]): Filter[Posts.Schema] =
    Filter.optional(userId)(_.userId === _)

  val cos = postUserIdFilter(None).tupled ** Filter.productEmpty[Posts.Schema]
  val tam = Filter.productEmpty[Posts.Schema].tupled

}

object Costam extends App {

  val cos: Query[(Posts.Schema, Users.Schema), (PostDAO, UserDAO), Seq] = for {
    posts <- Posts.table
    users <- Users.table if posts.userId === users.id
  } yield (posts, users)


  val filtered = cos
    .filteredBy(Seq(postUserIdFilter(None).tupled ++ Filter.sumEmpty[Users.Schema]))
    .result
    .statements

  filtered.foreach(println)
}
