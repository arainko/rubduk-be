package io.rubduk.domain.repositories.live

import io.rubduk.domain.repositories.UserRepository
import io.rubduk.infrastructure.models.{Limit, Offset, Page, RowCount, UserDAO, UserId}
import io.rubduk.infrastructure.tables.Users
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import io.rubduk.infrastructure.converters.IdConverter._
import slick.jdbc.PostgresProfile.api._
import zio.{Task, ZIO}

/*
The env: DatabaseProvider is our database layer that our methods will use
to execute SQL queries on.
 */
class UserRepositoryLive(env: DatabaseProvider) extends UserRepository.Service {

  /*
  Some words of caution, you should always and I mean ALWAYS

  --- !!! --- !!! --- !!!
  import slick.jdbc.PostgresProfile.api._
  --- !!! --- !!! --- !!!

  when writing Repository classes to even have access to the Slick methods,
  also pay a lot of attention to your imports,
  eg. ZIO.fromDBIO won't work if you don't
  import slick.interop.zio.syntax._
  also mapping to our custom Id types won't work without an
  import io.rubduk.infrastructure.converters.IdConverter._
  because it uses typeclasses to do this which in turn are implemented with
  implicits so they need the correct implicits in scope.
   */

  /*
  ZIO.fromDBIO takes a DBIO as an argument and turns it into a zio.Task.
  Think of DBIO like a SQL query but built with collection-like methods (filter, map, etc.).
  Have a look at this to understand what a Task, IO and ZIO are: https://zio.dev/docs/overview/overview_index
  Look thru the whole overview and I think you'll more or less get it.

  DBIO is built with our table element (declared for users as Users.table),
  you can execute a lot of familiar collection-like methods on it (filter, map etc.)
  to perform database queries, remember to always call .result at the end to fetch the result.

  We always call .provide(env) at the end to provide the proper Database to our method to
  execute the query.
   */
  override def getById(userId: UserId): Task[Option[UserDAO]] =
    ZIO.fromDBIO {
      Users.table
        .filter(_.id === userId)
        .result
        .headOption
    }.provide(env)

  override def getByEmail(email: String): Task[Option[UserDAO]] =
    ZIO.fromDBIO {
      Users.table
        .filter(_.email === email)
        .result
        .headOption
    }.provide(env)

  /*
  .zipPar executes the two methods in parallel so we won't have to wait that
  long for the results to come from the DB
   */
  override def getAllPaginated(offset: Offset, limit: Limit): Task[Page[UserDAO]] =
    getAll(offset, limit).zipPar(count).map {
      case (users, userCount) => Page(users, userCount)
    }

  override def getAll(offset: Offset, limit: Limit): Task[Seq[UserDAO]] =
    ZIO.fromDBIO {
      Users.table
        .drop(offset.value)
        .take(limit.value)
        .result
    }.provide(env)

  override def count: Task[RowCount] =
    ZIO.fromDBIO {
      Users.table.length.result
    }.provide(env)

  override def insert(user: UserDAO): Task[UserId] =
    ZIO.fromDBIO {
      Users.table.returning(Users.table.map(_.id)) += user
    }.provide(env)

  override def update(userId: UserId, user: UserDAO): Task[RowCount] =
    ZIO.fromDBIO {
      Users.table
        .map(u => (u.name, u.lastName, u.dateOfBirth))
        .update((user.name, user.lastName, user.dateOfBirth))
    }.provide(env)
}
