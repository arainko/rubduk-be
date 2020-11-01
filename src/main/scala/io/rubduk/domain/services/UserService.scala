package io.rubduk.domain.services

import cats.syntax.functor._
import io.rubduk.domain.UserRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.errors.UserError
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.models.Page._
import io.rubduk.infrastructure.models._
import zio.ZIO

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

  def insert(user: UserDTO): ZIO[UserRepository, Nothing, UserId] =
    UserRepository.insert(user.toDAO)
      .orDieWith(ServerError)

  def update(userId: UserId, user: UserDTO): ZIO[UserRepository, UserError, Unit] =
    UserRepository.update(userId, user.toDAO)
      .reject { case 0 => UserNotFound }
      .refineToOrDie[UserError]
      .unit
}
