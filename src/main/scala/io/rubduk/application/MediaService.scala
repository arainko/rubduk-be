package io.rubduk.application

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError._
import io.rubduk.domain.models.aliases.RowCount
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.common.{Limit, Offset, Page}
import io.rubduk.domain.models.media._
import io.rubduk.domain.models.user.UserId
import io.rubduk.domain.repositories.{MediaReadRepository, MediaRepository}
import io.rubduk.domain.typeclasses.syntax.BoolAlgebraOps
import io.rubduk.domain.{MediaApi, MediaReadRepository, MediaRepository, TokenValidation, UserRepository}
import zio.ZIO
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
    MediaReadRepository.getPaginated(
      offset,
      limit,
      MediaFilter.ByUser(userId).lift
    )

  def delete(mediumId: MediumId): ZIO[MediaRepository, ServerError, RowCount] =
    MediaRepository.delete(mediumId)

}
