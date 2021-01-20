package io.rubduk.domain.repositories

import io.rubduk.domain.FriendRequestRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases.RowCount
import io.rubduk.domain.models.friendrequest._
import io.rubduk.infrastructure.repositories.FriendRequestRepositoryLive
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZLayer}
import zio.macros.accessible

@accessible
object FriendRequestRepository {
  trait Service {
    def getById(friendRequestId: FriendRequestId): IO[ServerError, Option[FriendRequestRecord]]
    def insert(friendRequest: FriendRequestInRecord): IO[ServerError, FriendRequestId]
    def update(friendRequestId: FriendRequestId, status: FriendRequestStatus): IO[ServerError, RowCount]
    def delete(friendRequestId: FriendRequestId): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, FriendRequestRepository] =
    ZLayer.fromFunction(new FriendRequestRepositoryLive(_))
}
