package io.rubduk.infrastructure.repositories

import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.user._
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.filters.syntax._
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.tables.Users
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class UserRepositoryLive(env: DatabaseProvider) extends UserRepository.Service {

  override def getById(userId: UserId): IO[ServerError, Option[UserRecord]] =
    ZIO
      .fromDBIO {
        Users.table
          .filter(_.id === userId)
          .result
          .headOption
      }
      .mapError(ServerError)
      .provide(env)

  override def getByEmail(email: String): IO[ServerError, Option[UserRecord]] =
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
    filters: BoolAlgebra[UserFilter]
  ): IO[ServerError, Page[UserRecord]] =
    getAll(offset, limit, filters).zipPar(countFiltered(filters)).map {
      case (users, userCount) => Page(users, userCount)
    }

  override def getAll(offset: Offset, limit: Limit, filters: BoolAlgebra[UserFilter]): IO[ServerError, Seq[UserRecord]] =
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

  override def countFiltered(filters: BoolAlgebra[UserFilter]): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Users.table.filteredBy(filters).length.result
      }
      .mapError(ServerError)
      .provide(env)

  override def insert(user: UserRecord): IO[ServerError, UserId] =
    ZIO
      .fromDBIO {
        Users.table.returning(Users.table.map(_.id)) += user
      }
      .mapError(ServerError)
      .provide(env)

  override def update(userId: UserId, user: UserRecord): IO[ServerError, RowCount] =
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
