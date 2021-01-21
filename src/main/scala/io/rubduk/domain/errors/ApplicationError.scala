package io.rubduk.domain.errors

import scala.util.control.NoStackTrace

trait ApplicationError extends NoStackTrace

object ApplicationError {
  sealed trait DomainError                          extends ApplicationError
  final case class ThirdPartyError(message: String) extends ApplicationError
  final case class ServerError(message: String)     extends ApplicationError
  final case class ValidationError(message: String) extends ApplicationError

  case object AuthenticationError          extends DomainError
  case object CommentNotFound              extends DomainError
  case object CommentNotUnderPost          extends DomainError
  case object CommentNotByThisUser         extends DomainError
  case object FriendRequestNotFound        extends DomainError
  case object FriendRequestRejected        extends DomainError
  case object FriendRequestPending         extends DomainError
  case object FriendRequestNotPending      extends DomainError
  case object FriendRequestAlreadyApproved extends DomainError
  case object FriendRequestSentByYourself  extends DomainError
  case object PostNotFound                 extends DomainError
  case object PostNotByThisUser            extends DomainError
  case object UserNotFound                 extends DomainError
  case object UserAlreadyExists            extends DomainError
  case object MediumNotFound               extends DomainError

  object ServerError extends (Throwable => ServerError) {
    override def apply(throwable: Throwable): ServerError = ServerError(throwable.getMessage)
  }

  object ThirdPartyError extends (Throwable => ThirdPartyError) {
    override def apply(v1: Throwable): ThirdPartyError = new ThirdPartyError(v1.getMessage)
  }
}
