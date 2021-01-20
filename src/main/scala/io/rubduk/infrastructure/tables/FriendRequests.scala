package io.rubduk.infrastructure.tables

import io.rubduk.domain.models.friendrequest._
import io.rubduk.domain.models.user.UserId

import java.time.OffsetDateTime

object FriendRequests {
  import io.rubduk.infrastructure.SlickPGProfile.api._
  import io.rubduk.infrastructure.mappers._

  final case class Schema(tag: Tag) extends Table[FriendRequestRecord](tag, "friend_requests") {
    def id         = column[FriendRequestId]("id", O.AutoInc, O.PrimaryKey)
    def fromUserId = column[UserId]("from_user_id")
    def toUserId   = column[UserId]("to_user_id")
    def status     = column[FriendRequestStatus]("status")
    def dateAdded  = column[OffsetDateTime]("date_added")
    def *          = (id, fromUserId, toUserId, status, dateAdded).mapTo[FriendRequestRecord]
  }

  val table = TableQuery[Schema]
}
