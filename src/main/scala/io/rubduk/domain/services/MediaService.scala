package io.rubduk.domain.services

import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.repositories.{MediaReadRepository, MediaRepository}
import io.rubduk.domain.{MediaApi, MediaReadRepository, MediaRepository, TokenValidation, UserRepository}
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.models.media.{Base64Image, Medium, MediumId, MediumInRecord}
import io.rubduk.domain.models._
import io.rubduk.infrastructure.tables.Media
import io.rubduk.domain.typeclasses.IdConverter._
import io.rubduk.infrastructure.Filter
import zio.ZIO
import zio.clock._

object MediaService {

  def insert(
    idToken: IdToken,
    image: Base64Image
  ): ZIO[
    MediaRepository with Clock with MediaApi with UserRepository with TokenValidation,
    ApplicationError,
    MediumId
  ] =
    for {
      tokenUser <- TokenValidation.validateToken(idToken)
      userId <- UserService.getByEmail(tokenUser.email).map(_.id).someOrFail(UserNotFound)
      uploadedImage <- MediaApi.uploadImage(image)
      currentTime <- currentDateTime.orDie
      imageToInsert = MediumInRecord(userId, uploadedImage.link, currentTime)
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
        Filter.applicable[Media.Schema, UserId](userId)(_.userId === _) :: Nil
      )

    def delete(mediumId: MediumId): ZIO[MediaRepository, ServerError, RowCount] =
      MediaRepository.delete(mediumId)

}
