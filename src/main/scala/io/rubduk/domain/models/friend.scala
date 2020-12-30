package io.rubduk.domain.models

import io.rubduk.domain.models.user._

import java.time.OffsetDateTime

object friend {
  final case class FriendRequestId(value: Long) extends AnyVal

  final case class FriendRequestRecord(
    id: FriendRequestId,
    requesterId: UserId,
    requesteeId: UserId,
    dateAdded: OffsetDateTime
  )

  final case class FriendRequestInRecord(
    requesterId: UserId,
    requesteeId: UserId,
    dateAdded: OffsetDateTime
  )

  final case class FriendRequest(
    id: FriendRequestId,
    requester: User,
    requestee: User,
    dateAdded: OffsetDateTime
  )
}
