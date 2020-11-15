package io.rubduk.domain.services

import java.time.OffsetDateTime

import cats.syntax.option._
import cats.syntax.functor._
import io.rubduk.domain.UserRepository
import io.rubduk.domain.errors.ApplicationError
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.UserError.{UserAlreadyExists, UserNotFound}
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
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

  def getAllPaginated(offset: Offset, limit: Limit): ZIO[UserRepository, ServerError, Page[User]] =
    UserRepository
      .getAllPaginated(offset, limit)
      .map(_.map(_.toDomain))

  def getAll(offset: Offset, limit: Limit): ZIO[UserRepository, ServerError, Seq[User]] =
    UserRepository
      .getAll(offset, limit)
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
      fetchedByEmail <- UserRepository.getByEmail(user.email)
      originalCreatedOn = fetchedUser.createdOn
      _ <- UserRepository.update(userId, user.toDAO(originalCreatedOn))
      .whenM {
        fetchedByEmail.fold(ZIO.succeed(true)) { u =>
         if (u.id == userId.some) ZIO.succeed(true) else ZIO.fail(UserAlreadyExists)
        }
      }
    } yield ()
}
