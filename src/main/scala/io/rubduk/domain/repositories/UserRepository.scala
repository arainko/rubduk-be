package io.rubduk.domain.repositories

import io.rubduk.domain.UserRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.live.UserRepositoryLive
import io.rubduk.infrastructure.additional.Filter
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.tables.Users
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object UserRepository {

  trait Service {
    def getById(userId: UserId): IO[ServerError, Option[UserDAO]]

    def getByEmail(email: String): IO[ServerError, Option[UserDAO]]

    def getAllPaginated(
      offset: Offset,
      limit: Limit,
      filters: Seq[Filter[Users.Schema]]
    ): IO[ServerError, Page[UserDAO]]

    def getAll(offset: Offset, limit: Limit, filters: Seq[Filter[Users.Schema]]): IO[ServerError, Seq[UserDAO]]

    def countFiltered(filters: Seq[Filter[Users.Schema]]): IO[ServerError, RowCount]

    def insert(user: UserDAO): IO[ServerError, UserId]

    def update(userId: UserId, user: UserDAO): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, UserRepository] = ZLayer.fromFunction { database =>
    new UserRepositoryLive(database)
  }
}
