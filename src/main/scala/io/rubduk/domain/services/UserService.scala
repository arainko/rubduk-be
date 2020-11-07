package io.rubduk.domain.services

import java.time.OffsetDateTime

import cats.syntax.functor._
import io.rubduk.domain.UserRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.{ApplicationError, UserError}
import io.rubduk.domain.errors.UserError.{UserAlreadyExists, UserNotFound}
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import zio.{UIO, ZIO}

object UserService {

  def getById(userId: UserId): ZIO[UserRepository, UserError, User] =
    UserRepository.getById(userId)
      .flatMap(ZIO.fromOption(_))
      .bimap(_ => UserNotFound, _.toDomain)

  def getByEmail(email: String): ZIO[UserRepository, UserError, User] =
    UserRepository.getByEmail(email)
      .flatMap(ZIO.fromOption(_))
      .bimap(_ => UserNotFound, _.toDomain)

  def getAllPaginated(offset: Offset, limit: Limit): ZIO[UserRepository, Nothing, Page[User]] =
    UserRepository.getAllPaginated(offset, limit)
      .orDieWith(ServerError)
      .map(_.map(_.toDomain))

  def getAll(offset: Offset, limit: Limit): ZIO[UserRepository, Nothing, Seq[User]] =
    UserRepository.getAll(offset, limit)
      .orDieWith(ServerError)
      .map(_.map(_.toDomain))

  // TODO: Change errors to ApplicationError
  def insert(user: UserDTO): ZIO[UserRepository, Throwable, UserId] =
    UserRepository.getByEmail(user.email)
      .filterOrFail(_.isEmpty)(UserAlreadyExists)
      .flatMap { _ => UserRepository.insert(user.toDAO(OffsetDateTime.now)) }


  def update(userId: UserId, user: UserDTO): ZIO[UserRepository, UserError, Unit] = {
    for {
      fetchedUser <- UserService.getById(userId)
      originalCreatedOn = fetchedUser.createdOn
      _ <- UserRepository.update(userId, user.toDAO(originalCreatedOn)).orDieWith(ServerError)
    } yield ()
  }
}
