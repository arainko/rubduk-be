package io.rubduk.domain.services

import java.time.OffsetDateTime

import cats.syntax.functor._
import cats.syntax.option._
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.UserError.{UserAlreadyExists, UserNotFound}
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.{TokenValidation, UserRepository}
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.tables.Users
import zio.ZIO

object UserService {

  def getById(userId: UserId): ZIO[UserRepository, ApplicationError, User] =
    UserRepository
      .getById(userId)
      .someOrFail(UserNotFound)
      .map(_.toDomain)

  def getByEmail(email: String): ZIO[UserRepository, ApplicationError, User] =
    UserRepository
      .getByEmail(email)
      .someOrFail(UserNotFound)
      .map(_.toDomain)

  def loginOrRegister(idToken: IdToken): ZIO[TokenValidation with UserRepository, ApplicationError, User] =
    for {
      tokenUser <- TokenValidation.validateToken(idToken)
        .tapError(e => ZIO.succeed(println(e.message)))
      user <- getByEmail(tokenUser.email).orElse {
        insert(tokenUser.toDTO).flatMap(getById)
      }
    } yield user

  def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: Filter[Users.Schema]*
  ): ZIO[UserRepository, ServerError, Page[User]] =
    UserRepository
      .getAllPaginated(offset, limit, filters)
      .map(_.map(_.toDomain))

  def getAll(
    offset: Offset,
    limit: Limit,
    filters: Filter[Users.Schema]*
  ): ZIO[UserRepository, ServerError, Seq[User]] =
    UserRepository
      .getAll(offset, limit, filters)
      .map(_.map(_.toDomain))

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
}
