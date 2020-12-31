package io.rubduk.domain.errors

sealed trait FriendRequestError extends ApplicationError

object FriendRequestError {
  final case object FriendRequestNotFound        extends FriendRequestError
  final case object FriendRequestRejected        extends FriendRequestError
  final case object FriendRequestPending         extends FriendRequestError
  final case object FriendRequestAlreadyApproved extends FriendRequestError
}
