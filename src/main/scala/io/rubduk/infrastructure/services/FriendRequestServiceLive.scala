package io.rubduk.infrastructure.services

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.friendrequest._
import io.rubduk.domain.services.FriendRequestService
import io.rubduk.infrastructure.tables.{FriendRequests, Users}
import io.rubduk.infrastructure.filters.syntax._
import zio._
import slick.interop.zio._
import slick.interop.zio.syntax._

class FriendRequestServiceLive(env: DatabaseProvider) extends FriendRequestService.Service {
  import io.rubduk.infrastructure.SlickPGProfile.api._
  import io.rubduk.infrastructure.mappers._

  private def joinedRequests(filters: FriendRequestFilterAggregate) =
    for {
      reqs <- FriendRequests.table.filteredBy(filters.requestFilters.map(_.interpret))
      fromUser <- Users.table.filteredBy(filters.fromUserFilters.map(_.interpret)) if reqs.fromUserId === fromUser.id
      toUser <- Users.table.filteredBy(filters.toUserFilters.map(_.interpret)) if reqs.toUserId === toUser.id
    } yield (reqs, fromUser, toUser)

  override def getPaginated(
    offset: Offset,
    limit: Limit,
    filters: FriendRequestFilterAggregate
  ): IO[ApplicationError, Page[FriendRequest]] =
    getAll(offset, limit, filters).zipPar {
      ZIO.fromDBIO(joinedRequests(filters).length.result).mapError(ServerError)
    }
      .map((Page.apply[FriendRequest] _).tupled)
      .provide(env)

  override def getAll(
    offset: Offset,
    limit: Limit,
    filters: FriendRequestFilterAggregate
  ): IO[ApplicationError, Seq[FriendRequest]] =
    ZIO
      .fromDBIO {
        joinedRequests(filters)
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .bimap(
        ServerError,
        _.map { case (request, fromUser, toUser) => request.toDomain(fromUser, toUser) }
      )
      .provide(env)

  override def getSingle(filters: FriendRequestFilterAggregate): IO[ApplicationError, FriendRequest] =
    ZIO
      .fromDBIO {
        joinedRequests(filters)
          .take(1)
          .result
          .headOption
      }
      .bimap(
        ServerError,
        _.map { case (request, fromUser, toUser) => request.toDomain(fromUser, toUser) }
      )
      .unrefineTo[ApplicationError]
      .someOrFail(FriendRequestNotFound)
      .provide(env)
}
