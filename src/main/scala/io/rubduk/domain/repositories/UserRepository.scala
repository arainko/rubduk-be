package io.rubduk.domain.repositories

import io.rubduk.domain.UserRepository
import io.rubduk.domain.errors.ApplicationError.ServerError
import io.rubduk.domain.repositories.live.UserRepositoryLive
import io.rubduk.infrastructure.models._
import slick.interop.zio.DatabaseProvider
import zio.macros.accessible
import zio.{IO, URLayer, ZLayer}

/*
This annotation will generate a static method for each method of the UserRepository.Service trait,
we'll be able to use them if we .provideLayer of type UserRepository.Service.
 */
@accessible
object UserRepository {

  /*
  We do Service traits to easily test them after with a mock.
  Btw. what we do here is the so called 'module pattern'.
  Link: https://zio.dev/docs/howto/howto_use_layers

  Repositories should return and take DAO objects as arguments.

  Something about repositories in general,
  they are a very primitive classes which should
  only take care of one type of entity and with very
  general methods (eg. insert, getAll etc.) that let's us use
  them as 'building blocks' in a service (not the Service trait below).

  A service is a class that takes care of interactions between domain models,
  it's where you put your app logic.

  This right here is a repository not a service (don't pay attention to the name of the
  trait, it's just a best practice to name them like that).
   */
  trait Service {
    def getById(userId: UserId): IO[ServerError, Option[UserDAO]]
    def getByEmail(email: String): IO[ServerError, Option[UserDAO]]
    def getAllPaginated(offset: Offset, limit: Limit): IO[ServerError, Page[UserDAO]]
    def getAll(offset: Offset, limit: Limit): IO[ServerError, Seq[UserDAO]]
    def count: IO[ServerError, RowCount]
    def insert(user: UserDAO): IO[ServerError, UserId]
    def update(userId: UserId, user: UserDAO): IO[ServerError, RowCount]
  }

  /*
  Our live UserRepository layer, we'll use it in UserService in the future.
   */
  val live: URLayer[DatabaseProvider, UserRepository] = ZLayer.fromFunction { database =>
    new UserRepositoryLive(database)
  }

}
