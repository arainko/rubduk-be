package io.rubduk.application

import cats.syntax.option._
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.{ServerError, _}
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.common.{Limit, Offset, Page}
import io.rubduk.domain.models.media.MediumId
import io.rubduk.domain.models.user.{User, UserDTO, UserFilter, UserId}
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.{MediaRepository, TokenValidation, UserRepository}
import io.rubduk.domain.typeclasses.BoolAlgebra
import zio._

import java.time.OffsetDateTime

object UserService {

  def getById(userId: UserId): ZIO[UserRepository, ApplicationError, User] =
    UserRepository
      .getById(userId)
      .someOrFail(UserNotFound)

  def getByEmail(email: String): ZIO[UserRepository, ApplicationError, User] =
    UserRepository
      .getByEmail(email)
      .someOrFail(UserNotFound)

  def authenticate(idToken: IdToken): ZIO[TokenValidation with UserRepository, ApplicationError, User] =
    for {
      tokenUser <- TokenValidation.validateToken(idToken)
      user      <- getByEmail(tokenUser.email)
    } yield user

  def loginOrRegister(idToken: IdToken): ZIO[TokenValidation with UserRepository, ApplicationError, User] =
    for {
      tokenUser <- TokenValidation.validateToken(idToken)
      user <- getByEmail(tokenUser.email).orElse {
        insert(tokenUser.toDTO).flatMap(getById)
      }
    } yield user

  def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[UserFilter]
  ): ZIO[UserRepository, ServerError, Page[User]] =
    UserRepository
      .getAllPaginated(offset, limit, filters)

  def getAll(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[UserFilter]
  ): ZIO[UserRepository, ServerError, Seq[User]] =
    UserRepository
      .getAll(offset, limit, filters)

  def updateProfilePicture(
    idToken: IdToken,
    mediumId: MediumId
  ): ZIO[UserRepository with MediaRepository with TokenValidation with UserRepository, ApplicationError, Unit] =
    for {
      user   <- authenticate(idToken)
      userId <- ZIO.fromOption(user.id).orElseFail(UserNotFound)
      medium <- MediaService.getById(mediumId)
      _      <- ZIO.cond(medium.userId == userId, (), AuthenticationError)
      _      <- UserRepository.updateProfilePicture(userId, mediumId)
    } yield ()

  def insert(user: UserDTO): ZIO[UserRepository, ApplicationError, UserId] =
    UserRepository
      .getByEmail(user.email)
      .unrefineTo[ApplicationError]
      .filterOrFail(_.isEmpty)(UserAlreadyExists)
      .zipRight(UserRepository.insert(user.toDAO(OffsetDateTime.now)))

  def update(userId: UserId, user: UserDTO): ZIO[UserRepository, ApplicationError, Unit] =
    for {
      fetchedUser <- UserService.getById(userId)
      _ <-
        UserRepository
          .getByEmail(user.email)
          .unrefineTo[ApplicationError]
          .filterOrFail(_.fold(true)(_.id == userId.some))(UserAlreadyExists)
      originalCreatedOn = fetchedUser.createdOn
      _ <- UserRepository.update(userId, user.toDAO(originalCreatedOn))
    } yield ()

  def delete(idToken: IdToken): ZIO[TokenValidation with UserRepository, ApplicationError, Unit] =
    UserService
      .authenticate(idToken)
      .map(_.id)
      .someOrFail(UserNotFound)
      .flatMap(UserRepository.delete)
      .unit
}
