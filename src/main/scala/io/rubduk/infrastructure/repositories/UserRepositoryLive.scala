package io.rubduk.infrastructure.repositories

import cats.implicits.catsSyntaxOptionId
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.media.MediumId
import io.rubduk.domain.models.user._
import io.rubduk.domain.repositories.UserRepository
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.domain.typeclasses.syntax.BoolAlgebraOps
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.filters.syntax._
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.tables.{Media, Users}
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{IO, ZIO}

class UserRepositoryLive(env: DatabaseProvider) extends UserRepository.Service {

  private def joinedUser(filter: BoolAlgebra[UserFilter]) =
    for {
      (user, profilePic) <-
        Users.table
          .filteredBy(filter)
          .joinLeft(Media.table)
          .on(_.profilePicId === _.id)
    } yield (user, profilePic)

  override def getById(userId: UserId): IO[ServerError, Option[User]] =
    ZIO
      .fromDBIO {
        joinedUser(UserFilter.ById(userId).lift).result.headOption
      }
      .bimap(ServerError, _.map { case (user, pic) => user.toDomain(pic.map(_.toDomain)) })
      .provide(env)

  override def getByEmail(email: String): IO[ServerError, Option[User]] =
    ZIO
      .fromDBIO {
        joinedUser(UserFilter.ByEmail(email).lift).result.headOption
      }
      .bimap(ServerError, _.map { case (user, pic) => user.toDomain(pic.map(_.toDomain)) })
      .provide(env)

  override def getAllPaginated(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[UserFilter]
  ): IO[ServerError, Page[User]] =
    getAll(offset, limit, filters).zipPar(countFiltered(filters)).map {
      case (users, userCount) => Page(users, userCount)
    }

  override def getAll(
    offset: Offset,
    limit: Limit,
    filters: BoolAlgebra[UserFilter]
  ): IO[ServerError, Seq[User]] =
    ZIO
      .fromDBIO {
        joinedUser(filters)
          .drop(offset.value)
          .take(limit.value)
          .result
      }
      .bimap(ServerError, _.map { case (user, pic) => user.toDomain(pic.map(_.toDomain)) })
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

  override def updateProfilePicture(userId: UserId, mediumId: MediumId): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Users.table
          .filter(_.id === userId)
          .map(_.profilePicId)
          .update(mediumId.some)
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

  override def delete(userId: UserId): IO[ServerError, RowCount] =
    ZIO
      .fromDBIO {
        Users.table
          .filter(_.id === userId)
          .delete
      }
      .mapError(ServerError)
      .provide(env)
}
