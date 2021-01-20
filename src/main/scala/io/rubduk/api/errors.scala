package io.rubduk.api

import akka.http.interop.ErrorResponse
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.{AuthenticationError, CommentNotByThisUser, CommentNotFound, CommentNotUnderPost, DomainError, FriendRequestAlreadyApproved, FriendRequestNotFound, FriendRequestNotPending, FriendRequestPending, FriendRequestRejected, FriendRequestSentByYourself, PostNotByThisUser, PostNotFound, ServerError, ThirdPartyError, UserAlreadyExists, UserNotFound, ValidationError}

object errors {

  implicit val domainErrorResponse: ErrorResponse[DomainError] = {
    case AuthenticationError =>
      HttpResponse(StatusCodes.Unauthorized, entity = "You are not authorized for this action.")
    case CommentNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested comment was not found.")
    case CommentNotUnderPost =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested comment does not belong to this post.")
    case CommentNotByThisUser =>
      HttpResponse(StatusCodes.Unauthorized, entity = "User is not authorized to alter this comment.")
    case FriendRequestNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "The requested friend request couldn't be found.")
    case FriendRequestRejected =>
      HttpResponse(StatusCodes.BadRequest, entity = "The friend request has been rejected.")
    case FriendRequestPending =>
      HttpResponse(StatusCodes.BadRequest, entity = "The friend request is pending.")
    case FriendRequestNotPending =>
      HttpResponse(StatusCodes.BadRequest, entity = "The friend request is not pending.")
    case FriendRequestAlreadyApproved =>
      HttpResponse(StatusCodes.BadRequest, entity = "The friend request is already approved.")
    case FriendRequestSentByYourself =>
      HttpResponse(StatusCodes.BadRequest, entity = "You're trying to befriend yourself.")
    case PostNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested post was not found.")
    case PostNotByThisUser =>
      HttpResponse(StatusCodes.Unauthorized, entity = "User is not authorized to alter this post.")
    case UserNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested user was not found.")
    case UserAlreadyExists =>
      HttpResponse(StatusCodes.UnprocessableEntity, entity = "User with specified email already exists.")
  }

  implicit val applicationErrorResponse: ErrorResponse[ApplicationError] = {
    case ServerError(_)           => HttpResponse(StatusCodes.InternalServerError, entity = "A server error has occurred.")
    case ValidationError(message) => HttpResponse(StatusCodes.BadRequest, entity = message)
    case ThirdPartyError(_) =>
      HttpResponse(
        StatusCodes.ServiceUnavailable,
        entity = "A third party service is currently unavailable, try again later."
      )
    case domainError: DomainError => domainErrorResponse.toHttpResponse(domainError)
    case _ =>
      HttpResponse(
        StatusCodes.InternalServerError,
        entity = "An UNEXPECTED error has occurred, this should NEVER happen, call the maintainers."
      )
  }

}
