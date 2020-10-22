package io.rubduk.infrastructure.converters

import io.rubduk.infrastructure.models.{CommentId, PostId, UserId}
import slick.jdbc.PostgresProfile.api._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import scala.reflect.ClassTag

trait IdConverter[A] {
  def fromLong(value: Long): A
  def toLong(id: A): Long
}

object IdConverter {

  def apply[A: IdConverter]: IdConverter[A] = implicitly

  implicit val userIdConverter: IdConverter[UserId] = new IdConverter[UserId] {
    override def fromLong(value: Long): UserId = UserId(value)
    override def toLong(id: UserId): Long      = id.value
  }

  implicit val postIdConverter: IdConverter[PostId] = new IdConverter[PostId] {
    override def fromLong(value: Long): PostId = PostId(value)
    override def toLong(id: PostId): Long      = id.value
  }

  implicit val commentIdConverter: IdConverter[CommentId] = new IdConverter[CommentId] {
    override def fromLong(value: Long): CommentId = CommentId(value)
    override def toLong(id: CommentId): Long      = id.value
  }

  implicit def idMapper[A: IdConverter: ClassTag]: BaseColumnType[A] =
    MappedColumnType.base[A, Long](
      IdConverter[A].toLong,
      IdConverter[A].fromLong
    )

  def Id[A: IdConverter]: PathMatcher1[A] = LongNumber.map(IdConverter[A].fromLong)

}
