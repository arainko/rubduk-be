package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.Filter.FilterOps
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.domain.typeclasses.IdConverter._
import io.rubduk.domain.models._
import io.rubduk.infrastructure.Filter
import io.rubduk.infrastructure.tables.Users
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class UserRepositoryLive(env: DatabaseProvider) extends UserRepository.Service {

  override def getById(userId: UserId): IO[ServerError, Option[UserDAO]] =
    ZIO
      .fromDBIO {
        Users.table
          .filter(_.id === userId)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def getByEmail(email: String): IO[ServerError, Option[UserDAO]] =
    ZIO
      .fromDBIO {
        Users.table
          .filter(_.email === email)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: Seq[Filter[Users.Schema]]
  ): IO[ServerError, Page[UserDAO]] =
    getAll(offset, limit, filters).zipPar(countFiltered(filters)).map {
      case (users, userCount) => Page(users, userCount)
    }

  override def getAll(offset: Offset, limit: Limit, filters: Seq[Filter[Users.Schema]]): IO[ServerError, Seq[UserDAO]] =
    ZIO
      .fromDBIO {
        Users.table
          .filteredBy(filters)
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .mapError(ServerError)
      .provide(env)

  override def countFiltered(filters: Seq[Filter[Users.Schema]]): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Users.table.filteredBy(filters).length.result
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(user: UserDAO): IO[ServerError, UserId] =
    ZIO
      .fromDBIO {
        Users.table.returning(Users.table.map(_.id)) += user
      }
      .mapError(ServerError)
      .provide(env)

  override def update(userId: UserId, user: UserDAO): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Users.table
          .filter(_.id === userId)
          .map(u => (u.name, u.lastName, u.dateOfBirth))
          .update((user.name, user.lastName, user.dateOfBirth))
      }
      .mapError(ServerError)
      .provide(env)
}
