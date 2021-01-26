package io.rubduk.application

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.common.{Limit, Offset, Page}
import io.rubduk.domain.models.media._
import io.rubduk.domain.models.user.UserId
import io.rubduk.domain.repositories.MediaRepository
import io.rubduk.domain.services.MediaReadService
import io.rubduk.domain.typeclasses.syntax.BoolAlgebraOps
import io.rubduk.domain.{MediaApi, MediaReadRepository, MediaRepository, TokenValidation, UserRepository}
import zio._
import zio.clock.{Clock, currentDateTime}

object MediaService {

  def insert(
    idToken: IdToken,
    image: ImageRequest
  ): ZIO[
    MediaRepository with Clock with MediaApi with UserRepository with TokenValidation,
    ApplicationError,
    MediumId
  ] =
    for {
      tokenUser     <- TokenValidation.validateToken(idToken)
      userId        <- UserService.getByEmail(tokenUser.email).map(_.id).someOrFail(UserNotFound)
      uploadedImage <- MediaApi.uploadImage(image.base64Image)
      currentTime   <- currentDateTime.orDie
      imageToInsert = MediumInRecord(userId, uploadedImage.link, image.description, currentTime)
      insertedId <- MediaRepository.insert(imageToInsert)
    } yield insertedId

  def getByUserIdPaginated(
    userId: UserId,
    offset: Offset,
    limit: Limit
  ): ZIO[MediaReadRepository, ServerError, Page[Medium]] =
    MediaReadService.getPaginated(
      offset,
      limit,
      MediaFilter.ByUser(userId).lift
    )

  def getById(mediumId: MediumId): ZIO[MediaRepository, ApplicationError, Medium] =
    MediaRepository
      .getById(mediumId)
      .someOrFail(MediumNotFound)
      .map(_.toDomain)

  def delete(
    idToken: IdToken,
    mediumId: MediumId
  ): ZIO[MediaRepository with TokenValidation with UserRepository, ApplicationError, Unit] =
    for {
      userId <- UserService.authenticate(idToken).map(_.id).someOrFail(UserNotFound)
      _      <- MediaService.getById(mediumId).map(_.userId).filterOrFail(_ == userId)(AuthenticationError)
      _      <- MediaRepository.delete(mediumId)
    } yield ()
}
