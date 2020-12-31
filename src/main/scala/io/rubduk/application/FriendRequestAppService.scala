package io.rubduk.application

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.{TokenValidation, UserRepository}
import io.rubduk.domain.errors.FriendRequestError.{FriendRequestAlreadyApproved, FriendRequestPending, FriendRequestRejected}
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.friendrequest.FriendRequestFilter._
import io.rubduk.domain.models.friendrequest.FriendRequestStatus._
import io.rubduk.domain.models.friendrequest._
import io.rubduk.domain.models.user.UserId
import io.rubduk.domain.repositories.FriendRequestRepository
import io.rubduk.domain.services.FriendRequestService
import zio.{Has, ZIO}
import zio.clock._

object FriendRequestAppService {

  def insert(request: FriendRequestRequest, idToken: IdToken): ZIO[Has[FriendRequestRepository.Service] with Clock with Has[FriendRequestService.Service] with UserRepository with TokenValidation, ApplicationError, FriendRequestId] =
    for {
      fromUserId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _ <- UserService.getById(request.toUserId)
      validation <- verifyExistenceAndStatus(fromUserId, request.toUserId)
        .
      currentDate <- currentDateTime.orDie
      record = FriendRequestInRecord(fromUserId, request.toUserId, Pending, currentDate)
      insertedId <- FriendRequestRepository.insert(record)
    } yield insertedId

  private def verifyExistenceAndStatus(fromUserId: UserId, toUserId: UserId) =
    for {
      request <- FriendRequestService.getSingle {
        FriendRequestFilterAggregate(
          SentByUser(fromUserId) ::
          SentToUser(toUserId) ::
          Nil
        )
      }
      checked <- ZIO.succeed {
        request.status match {
          case Rejected => FriendRequestRejected
          case Accepted => FriendRequestAlreadyApproved
          case Pending => FriendRequestPending
        }
      }
    } yield checked
}
