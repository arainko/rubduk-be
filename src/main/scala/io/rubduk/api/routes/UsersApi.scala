package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as => parse}
import cats.syntax.functor._
import io.rubduk.api.serializers.unmarshallers.{limit, offset}
import io.rubduk.domain.{TokenValidation, UserRepository}
import io.rubduk.domain.services.{IdToken, UserService}
import io.rubduk.infrastructure.typeclasses.IdConverter.{Id, _}
import io.rubduk.infrastructure.models.{Limit, Offset, UserDTO, UserId}

object UsersApi {
  def apply(env: UserRepository with TokenValidation): Route = new UsersApi(env).routes
}

class UsersApi(env: UserRepository with TokenValidation) extends Api.Service {
  import io.rubduk.api.serializers.codecs._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.rubduk.domain.errors._

  override def routes: Route =
    pathPrefix("api" / "users") {
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
//        entity(parse[UserDTO]) { user =>
//          pathEnd {
//            complete {
//              UserService
//                .insert(user)
//                .provide(env)
//            }
//          }
//        } ~
        entity(parse[IdToken]) { token =>
          pathEnd {
            complete {
              UserService
                .loginOrRegister(token)
                .map(_.toDTO)
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
