package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.aliases.RowCount
import io.rubduk.domain.models.friendrequest._
import io.rubduk.domain.repositories.FriendRequestRepository
import io.rubduk.infrastructure.tables.FriendRequests
import zio._
import slick.interop.zio._
import slick.interop.zio.syntax._

class FriendRequestRepositoryLive(env: DatabaseProvider) extends FriendRequestRepository.Service {
  import io.rubduk.infrastructure.SlickPGProfile.api._
  import io.rubduk.infrastructure.mappers._

  override def getById(friendRequestId: FriendRequestId): IO[ServerError, Option[FriendRequestRecord]] =
    ZIO.fromDBIO {
      FriendRequests.table
        .filter(_.id === friendRequestId)
        .result
        .headOption
    }
      .mapError(ServerError)
      .provide(env)

  override def insert(friendRequest: FriendRequestInRecord): IO[ServerError, FriendRequestId] =
    ZIO.fromDBIO {
      FriendRequests.table
        .returning(FriendRequests.table.map(_.id)) += friendRequest.toOutRecord(FriendRequestId(0L))
    }
      .mapError(ServerError)
      .provide(env)

  override def update(friendRequestId: FriendRequestId, status: FriendRequestStatus): IO[ServerError, RowCount] =
    ZIO.fromDBIO {
      FriendRequests.table
        .map(_.status)
        .update(status)
    }
      .mapError(ServerError)
      .provide(env)

  override def delete(friendRequestId: FriendRequestId): IO[ServerError, RowCount] =
    ZIO.fromDBIO {
      FriendRequests.table
        .filter(_.id === friendRequestId)
        .delete
    }
      .mapError(ServerError)
      .provide(env)
}
