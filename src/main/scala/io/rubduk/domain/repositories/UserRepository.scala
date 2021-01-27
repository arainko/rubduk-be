package io.rubduk.domain.repositories

import io.rubduk.domain.UserRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.models.aliases._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.media.MediumId
import io.rubduk.domain.models.user._
import io.rubduk.domain.typeclasses.BoolAlgebra
import io.rubduk.infrastructure.repositories.UserRepositoryLive
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

@accessible
object UserRepository {

  trait Service {
    def getById(userId: UserId): IO[ServerError, Option[User]]

    def getByEmail(email: String): IO[ServerError, Option[User]]

    def getAllPaginated(
      offset: Offset,
      limit: Limit,
      filters: BoolAlgebra[UserFilter]
    ): IO[ServerError, Page[User]]

    def getAll(offset: Offset, limit: Limit, filters: BoolAlgebra[UserFilter]): IO[ServerError, Seq[User]]

    def countFiltered(filters: BoolAlgebra[UserFilter]): IO[ServerError, RowCount]

    def insert(user: UserRecord): IO[ServerError, UserId]

    def update(userId: UserId, user: UserRecord): IO[ServerError, RowCount]

    def delete(userId: UserId): IO[ServerError, RowCount]

    def updateProfilePicture(userId: UserId, mediumId: MediumId): IO[ServerError, RowCount]
  }

  val live: URLayer[DatabaseProvider, UserRepository] = ZLayer.fromFunction { database =>
    new UserRepositoryLive(database)
  }
}
