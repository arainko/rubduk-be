package io.rubduk.domain

import akka.http.interop.ErrorResponse
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.CommentError.{CommentNotByThisUser, CommentNotFound, CommentNotUnderPost}
import io.rubduk.domain.errors.PostError.{PostNotByThisUser, PostNotFound}
import io.rubduk.domain.errors.UserError.{UserAlreadyExists, UserNotFound}

package object errors {

  implicit val applicationErrorResponse: ErrorResponse[ApplicationError] = {
    case ServerError(_) => HttpResponse(StatusCodes.InternalServerError, entity = "A server error has occurred.")

    case ValidationError(message) => HttpResponse(StatusCodes.BadRequest, entity = message)

    case AuthenticationError =>
      HttpResponse(StatusCodes.Unauthorized, entity = "You are not authorized for this action.")

    case CommentNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested comment was not found.")

    case CommentNotUnderPost =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested comment does not belong to this post.")

    case CommentNotByThisUser =>
      HttpResponse(StatusCodes.Unauthorized, entity = "User is not authorized to alter this comment.")

    case PostNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested post was not found.")

    case PostNotByThisUser =>
      HttpResponse(StatusCodes.Unauthorized, entity = "User is not authorized to alter this post.")

    case UserNotFound =>
      HttpResponse(StatusCodes.NotFound, entity = "Requested user was not found.")

    case UserAlreadyExists =>
      HttpResponse(StatusCodes.UnprocessableEntity, entity = "User with specified email already exists.")

    case _ => HttpResponse(StatusCodes.InternalServerError, entity = "An unexpected error has occurred.")
  }
}
