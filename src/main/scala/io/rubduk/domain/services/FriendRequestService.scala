package io.rubduk.domain.services

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.friendrequest.{FriendRequest, FriendRequestFilterAggregate}
import io.rubduk.infrastructure.services.FriendRequestServiceLive
import slick.interop.zio.DatabaseProvider
import zio.{Has, IO, URLayer, ZLayer}
import zio.macros.accessible

@accessible
object FriendRequestService {

  trait Service {

    def getSingle(filters: FriendRequestFilterAggregate): IO[ApplicationError, FriendRequest]

    def getPaginated(
      offset: Offset,
      limit: Limit,
      filters: FriendRequestFilterAggregate
    ): IO[ApplicationError, Page[FriendRequest]]

    def getAll(
      offset: Offset,
      limit: Limit,
      filters: FriendRequestFilterAggregate
    ): IO[ApplicationError, Seq[FriendRequest]]

    def getAllUnbounded(
      filters: FriendRequestFilterAggregate
    ): IO[ApplicationError, Seq[FriendRequest]]

  }

  val live: URLayer[DatabaseProvider, Has[FriendRequestService.Service]] =
    ZLayer.fromFunction(new FriendRequestServiceLive(_))

}
