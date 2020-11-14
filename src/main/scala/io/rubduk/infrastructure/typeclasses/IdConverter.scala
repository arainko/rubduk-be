package io.rubduk.infrastructure.typeclasses

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import io.rubduk.infrastructure.additional.ImprovedPostgresProfile.api._
import io.rubduk.infrastructure.models.{CommentId, PostId, UserId}

import scala.reflect.ClassTag

trait IdConverter[A] {
  def fromLong(value: Long): A
  def toLong(id: A): Long
}

object IdConverter {

  def apply[A: IdConverter]: IdConverter[A] = implicitly

  def make[A](fromLongF: Long => A, toLongF: A => Long): IdConverter[A] =
    new IdConverter[A] {
      override def fromLong(value: Long): A = fromLongF(value)
      override def toLong(id: A): Long      = toLongF(id)
    }

  implicit val userIdConverter: IdConverter[UserId]       = make(UserId, _.value)
  implicit val postIdConverter: IdConverter[PostId]       = make(PostId, _.value)
  implicit val commentIdConverter: IdConverter[CommentId] = make(CommentId, _.value)

  implicit def idMapper[A: IdConverter: ClassTag]: BaseColumnType[A] =
    MappedColumnType.base[A, Long](
      IdConverter[A].toLong,
      IdConverter[A].fromLong
    )

  def Id[A: IdConverter]: PathMatcher1[A] = LongNumber.map(IdConverter[A].fromLong)
}
