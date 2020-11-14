package io.rubduk.api.routes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{ as => parse }
import cats.syntax.functor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.api.serializers.Unmarshallers.{ limit, offset }
import io.rubduk.domain.UserRepository
import io.rubduk.domain.services.UserService
import io.rubduk.infrastructure.converters.IdConverter.{ Id, _ }
import io.rubduk.infrastructure.models.{ Limit, Offset, UserDTO, UserId }

object UsersApi {
  def apply(env: UserRepository): Route = new UsersApi(env).routes
}

class UsersApi(env: UserRepository) extends Api.Service {
  override def routes: Route = pathPrefix("api" / "users") {
    get {
      parameters(
        "offset".as(offset) ? Offset(0),
        "limit".as(limit) ? Limit(10)
      ) { (offset, limit) =>
        pathEnd {
          complete {
            UserService
              .getAllPaginated(offset, limit)
              .map(_.map(_.toDTO))
              .provide(env)
          }
        }
      } ~ path(Id[UserId]) { userId =>
        pathEnd {
          complete {
            UserService
              .getById(userId)
              .map(_.toDTO)
              .provide(env)
          }
        }
      }
    } ~ post {
      entity(parse[UserDTO]) { user =>
        pathEnd {
          complete {
            UserService
              .insert(user)
              .provide(env)
          }
        }
      }
    } ~ put {
      (path(Id[UserId]) & entity(parse[UserDTO])) { (userId, user) =>
        pathEnd {
          complete {
            UserService
              .update(userId, user)
              .provide(env)
          }
        }
      }
    }
  }
}
