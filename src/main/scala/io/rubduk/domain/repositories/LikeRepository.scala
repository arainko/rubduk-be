package io.rubduk.domain.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases.RowCount
import io.rubduk.domain.models.post.{Like, PostId}
import io.rubduk.domain.models.user.UserId
import io.rubduk.infrastructure.tables.Likes
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import slick.jdbc.PostgresProfile.api._
import io.rubduk.infrastructure.mappers._
import zio._
import zio.macros.accessible

@accessible
object LikeRepository {

  trait Service {
    def addLike(postId: PostId, userId: UserId): IO[ServerError, RowCount]
    def removeLike(postId: PostId, userId: UserId): IO[ServerError, RowCount]
    def getByPostAndUserId(postId: PostId, userId: UserId): IO[ServerError, Option[Like]]
    def count(postId: PostId): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, Has[LikeRepository.Service]] =
    ZLayer.fromFunction { env =>
      new Service {
        override def getByPostAndUserId(postId: PostId, userId: UserId): IO[ServerError, Option[Like]] =
          ZIO
            .fromDBIO {
              Likes.table
                .filter(like => like.postId === postId && like.userId === userId)
                .result
                .headOption
            }
            .mapError(ServerError)
            .provide(env)

        override def addLike(postId: PostId, userId: UserId): IO[ServerError, RowCount] =
          ZIO
            .fromDBIO {
              Likes.table += Like(postId, userId)
            }
            .mapError(ServerError)
            .provide(env)

        override def removeLike(postId: PostId, userId: UserId): IO[ServerError, RowCount] =
          ZIO
            .fromDBIO {
              Likes.table
                .filter(like => like.postId === postId && like.userId === userId)
                .delete
            }
            .mapError(ServerError)
            .provide(env)

        override def count(postId: PostId): IO[ServerError, RowCount] =
          ZIO
            .fromDBIO {
              Likes.table
                .filter(_.postId === postId)
                .length
                .result
            }
            .mapError(ServerError)
            .provide(env)
      }
    }
}
