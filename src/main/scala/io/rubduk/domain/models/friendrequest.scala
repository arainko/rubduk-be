package io.rubduk.domain.models

import enumeratum._
import io.scalaland.chimney.dsl._
import io.rubduk.domain.models.user._
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.domain.typeclasses.BoolAlgebra.True

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
    requestFilters: BoolAlgebra[FriendRequestFilter] = True,
    fromUserFilters: BoolAlgebra[UserFilter] = True,
    toUserFilters: BoolAlgebra[UserFilter] = True
  )

  final case class FriendRequestRecord(
    id: FriendRequestId,
    fromUserId: UserId,
    toUserId: UserId,
    status: FriendRequestStatus,
    dateAdded: OffsetDateTime
  ) {

    def toDomain(fromUser: User, toUser: User): FriendRequest =
      this
        .into[FriendRequest]
        .withFieldConst(_.fromUser, fromUser)
        .withFieldConst(_.toUser, toUser)
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
  ) {

    def toFriend(user: UserDTO): FriendDTO =
      this
        .into[FriendDTO]
        .withFieldConst(_.friend, user)
        .transform
  }

  final case class FriendDTO(
    id: FriendRequestId,
    friend: UserDTO,
    dateAdded: OffsetDateTime
  )

  final case class FriendRequestRequest(
    toUserId: UserId
  )
}
