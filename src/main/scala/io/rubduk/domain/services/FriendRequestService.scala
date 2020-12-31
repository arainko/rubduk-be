package io.rubduk.domain.services

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.friendrequest.{FriendRequest, FriendRequestFilterAggregate}
import zio.IO
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

  }

}
