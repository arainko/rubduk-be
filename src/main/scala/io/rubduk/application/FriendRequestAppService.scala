package io.rubduk.application

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.common.{Limit, Offset, Page}
import io.rubduk.domain.models.friendrequest.FriendRequestFilter._
import io.rubduk.domain.models.friendrequest.FriendRequestStatus._
import io.rubduk.domain.models.friendrequest._
import io.rubduk.domain.models.user.{UserFilter, UserId}
import io.rubduk.domain.repositories.{FriendRequestRepository, LikeRepository}
import io.rubduk.domain.services.{FriendRequestService, MediaReadService}
import io.rubduk.domain.{PostRepository, TokenValidation, UserRepository}
import Page._
import cats.syntax.functor._
import io.rubduk.domain.models.media.{MediaFilter, MediumDTO}
import io.rubduk.domain.models.post
import io.rubduk.domain.models.post.PostFilter
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.domain.typeclasses.BoolAlgebra.{False, True}
import io.rubduk.domain.typeclasses.syntax.BoolAlgebraOps
import zio.clock._
import zio.{Has, ZIO}

object FriendRequestAppService {

  def insert(
    request: FriendRequestRequest,
    idToken: IdToken
  ): ZIO[Has[FriendRequestRepository.Service] with Clock with Has[
    FriendRequestService.Service
  ] with UserRepository with TokenValidation, ApplicationError, FriendRequestId] =
    for {
      fromUserId  <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _           <- UserService.getById(request.toUserId)
      _           <- validateInsertRequest(fromUserId, request.toUserId)
      currentDate <- currentDateTime.orDie
      record = FriendRequestInRecord(fromUserId, request.toUserId, Pending, currentDate)
      insertedId <- FriendRequestRepository.insert(record)
    } yield insertedId

  def acceptRequest(
    requestId: FriendRequestId,
    idToken: IdToken
  ): ZIO[Has[FriendRequestRepository.Service] with TokenValidation with UserRepository, ApplicationError, Unit] =
    for {
      request  <- FriendRequestRepository.getById(requestId).someOrFail(FriendRequestNotFound)
      toUserId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _        <- ZIO.cond(request.status == Pending, (), FriendRequestNotPending)
      _        <- ZIO.cond(request.toUserId == toUserId, (), AuthenticationError)
      _        <- FriendRequestRepository.update(requestId, Accepted)
    } yield ()

  def rejectRequest(
    requestId: FriendRequestId,
    idToken: IdToken
  ): ZIO[Has[FriendRequestRepository.Service] with TokenValidation with UserRepository, ApplicationError, Unit] =
    for {
      request  <- FriendRequestRepository.getById(requestId).someOrFail(FriendRequestNotFound)
      toUserId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _        <- ZIO.cond(request.status == Pending, (), FriendRequestNotPending)
      _        <- ZIO.cond(request.toUserId == toUserId, (), AuthenticationError)
      _        <- FriendRequestRepository.update(requestId, Rejected)
    } yield ()

  def getPending(
    idToken: IdToken,
    offset: Offset,
    limit: Limit,
    filters: FriendRequestFilterAggregate
  ): ZIO[Has[FriendRequestService.Service] with TokenValidation with UserRepository, ApplicationError, Page[
    FriendRequestDTO
  ]] =
    for {
      userId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      pendingRequests <- getRequests(
        offset,
        limit,
        filters.requestFilters &&& SentToUser(userId) &&& WithStatus(Pending)
      )
    } yield pendingRequests.map(_.toDTO)

  def getSent(
    idToken: IdToken,
    offset: Offset,
    limit: Limit,
    filters: FriendRequestFilterAggregate
  ): ZIO[Has[FriendRequestService.Service] with TokenValidation with UserRepository, ApplicationError, Page[
    FriendRequestDTO
  ]] =
    for {
      userId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      sentRequests <- getRequests(
        offset,
        limit,
        filters.requestFilters &&& SentByUser(userId) &&& WithStatus(Pending)
      )
    } yield sentRequests.map(_.toDTO)

  def getFriends(
    idToken: IdToken,
    offset: Offset,
    limit: Limit,
    filters: FriendRequestFilterAggregate
  ): ZIO[Has[FriendRequestService.Service] with TokenValidation with UserRepository, ApplicationError, Page[
    FriendRequestDTO
  ]] =
    for {
      userId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      acceptedRequests <- getRequests(
        offset,
        limit,
        filters.requestFilters &&& (SentToUser(userId).lift ||| SentByUser(userId)) &&& WithStatus(Accepted)
      )
    } yield acceptedRequests.map(_.toDTO)

  def getFriendPostFeed(
    token: IdToken,
    offset: Offset,
    limit: Limit
  ): ZIO[PostRepository with UserRepository with Has[
    FriendRequestService.Service
  ] with TokenValidation with Has[LikeRepository.Service], ApplicationError, Page[post.PostDTO]] =
    for {
      userId <- UserService.authenticate(token).map(_.id).someOrFail(UserNotFound)
      acceptedRequests <- FriendRequestService.getAllUnbounded(
        FriendRequestFilterAggregate(
          (SentToUser(userId).lift ||| SentByUser(userId)) &&& WithStatus(Accepted)
        )
      )
      onlyFriends = processFriends(acceptedRequests, userId).flatMap(_.friend.id)
      postFilters =
        onlyFriends
          .map(PostFilter.ByUser)
          .map(_.lift)
          .reduceOption(_ ||| _)
          .getOrElse(False) ||| PostFilter.ByUser(userId)
      posts <- PostService.getAllPaginated(offset, limit, postFilters)
    } yield posts.map(_.toDTO)

  def getFriendMediaFeed(
    token: IdToken,
    offset: Offset,
    limit: Limit
  ): ZIO[Has[MediaReadService.Service] with Has[FriendRequestService.Service] with TokenValidation with UserRepository, ApplicationError, Page[MediumDTO]] =
    for {
      userId <- UserService.authenticate(token).map(_.id).someOrFail(UserNotFound)
      acceptedRequests <- FriendRequestService.getAllUnbounded(
        FriendRequestFilterAggregate(
          (SentToUser(userId).lift ||| SentByUser(userId)) &&& WithStatus(Accepted)
        )
      )
      onlyFriends = processFriends(acceptedRequests, userId).flatMap(_.friend.id)
      mediaFilters =
        onlyFriends
          .map(MediaFilter.ByUser)
          .map(_.lift)
          .reduceOption(_ ||| _)
          .getOrElse(False) ||| MediaFilter.ByUser(userId)
      media <- MediaReadService.getPaginated(offset, limit, mediaFilters)
    } yield media.map(_.toDTO)

  private def getRequests(
    offset: Offset,
    limit: Limit,
    requestFilters: BoolAlgebra[FriendRequestFilter],
    fromUserFilters: BoolAlgebra[UserFilter] = True,
    toUserFilters: BoolAlgebra[UserFilter] = True
  ) =
    FriendRequestService.getPaginated(
      offset,
      limit,
      FriendRequestFilterAggregate(
        requestFilters,
        fromUserFilters,
        toUserFilters
      )
    )

  private def processFriends(requests: Seq[FriendRequest], userId: UserId) = {
    val friendRequests = requests
    val friends = friendRequests.foldLeft(Seq.empty[FriendDTO]) { (acc, curr) =>
      acc :+
        curr.toDTO.toFriend(curr.fromUser.toDTO) :+
        curr.toDTO.toFriend(curr.toUser.toDTO)
    }
    friends.filter(_.friend.id != Some(userId))
  }

  private def validateInsertRequest(fromUserId: UserId, toUserId: UserId) =
    for {
      _ <- ZIO.cond(fromUserId != toUserId, (), FriendRequestSentByYourself)
      _ <-
        FriendRequestService
          .getSingle {
            FriendRequestFilterAggregate(
              (SentByUser(fromUserId).lift &&& SentToUser(toUserId)) |||
                (SentByUser(toUserId).lift &&& SentToUser(fromUserId))
            )
          }
          .option
          .filterOrFail(_.isEmpty)(FriendRequestPending)

    } yield ()
}
