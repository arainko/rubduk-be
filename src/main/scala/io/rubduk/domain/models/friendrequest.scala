package io.rubduk.domain.models

import enumeratum._
import io.scalaland.chimney.dsl._
import io.rubduk.domain.models.user._

import java.time.OffsetDateTime

object friendrequest {
  final case class FriendRequestId(value: Long) extends AnyVal

  sealed trait FriendRequestStatus extends EnumEntry

  object FriendRequestStatus extends Enum[FriendRequestStatus] {
    case object Rejected extends FriendRequestStatus
    case object Accepted extends FriendRequestStatus
    case object Pending  extends FriendRequestStatus

    override def values: IndexedSeq[FriendRequestStatus] = findValues
  }

  sealed trait FriendRequestFilter

  object FriendRequestFilter {
    final case class WithStatus(status: FriendRequestStatus) extends FriendRequestFilter
    final case class SentByUser(userId: UserId)              extends FriendRequestFilter
    final case class SentToUser(userId: UserId)              extends FriendRequestFilter
  }

  final case class FriendRequestFilterAggregate(
    requestFilters: Seq[FriendRequestFilter] = Seq.empty,
    fromUserFilters: Seq[UserFilter] = Seq.empty,
    toUserFilters: Seq[UserFilter] = Seq.empty
  )

  final case class FriendRequestRecord(
    id: FriendRequestId,
    fromUserId: UserId,
    toUserId: UserId,
    status: FriendRequestStatus,
    dateAdded: OffsetDateTime
  ) {

    def toDomain(fromUser: UserRecord, toUser: UserRecord): FriendRequest =
      this
        .into[FriendRequest]
        .withFieldConst(_.fromUser, fromUser.toDomain)
        .withFieldConst(_.toUser, toUser.toDomain)
        .transform
  }

  final case class FriendRequestInRecord(
    fromUserId: UserId,
    toUserId: UserId,
    status: FriendRequestStatus,
    dateAdded: OffsetDateTime
  ) {

    def toOutRecord(id: FriendRequestId): FriendRequestRecord =
      this
        .into[FriendRequestRecord]
        .withFieldConst(_.id, id)
        .transform
  }

  final case class FriendRequest(
    id: FriendRequestId,
    fromUser: User,
    toUser: User,
    status: FriendRequestStatus,
    dateAdded: OffsetDateTime
  ) {

    def toDTO: FriendRequestDTO =
      this
        .into[FriendRequestDTO]
        .withFieldComputed(_.fromUser, _.fromUser.toDTO)
        .withFieldComputed(_.toUser, _.toUser.toDTO)
        .transform
  }

  final case class FriendRequestDTO(
    id: FriendRequestId,
    fromUser: UserDTO,
    toUser: UserDTO,
    status: FriendRequestStatus,
    dateAdded: OffsetDateTime
  )

  final case class FriendRequestRequest(
    toUserId: UserId
  )
}
