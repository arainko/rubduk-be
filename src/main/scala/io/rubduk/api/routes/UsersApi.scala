package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as => parse}
import cats.syntax.functor._
import io.rubduk.api.custom.AuthDirectives._
import io.rubduk.api.serializers.unmarshallers.{limit, offset}
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.services.{MediaService, UserService}
import io.rubduk.domain.{MediaApi, MediaReadRepository, MediaRepository, TokenValidation, UserRepository}
import io.rubduk.infrastructure.models._
import io.rubduk.infrastructure.models.media.Base64Image
import io.rubduk.infrastructure.typeclasses.IdConverter.{Id, _}
import zio.clock.Clock

object UsersApi {

  def apply(
    env: UserRepository with TokenValidation with MediaReadRepository with MediaRepository with MediaApi with Clock
  ): Route = new UsersApi(env).routes
}

class UsersApi(
  env: UserRepository with TokenValidation with MediaReadRepository with MediaRepository with MediaApi with Clock
) extends Api.Service {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.rubduk.api.serializers.codecs._
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
        } ~ path(Id[UserId] / "media") { userId =>
          parameters(
            "offset".as(offset) ? Offset(0),
            "limit".as(limit) ? Limit(10)
          ) { (offset, limit) =>
            pathEnd {
              complete {
                MediaService
                  .getByUserIdPaginated(userId, offset, limit)
                  .map(_.map(_.toDTO))
                  .provide(env)
              }
            }
          }
        }
      } ~ post {
        (path("login") & entity(parse[IdToken])) { token =>
          pathEnd {
            complete {
              UserService
                .loginOrRegister(token)
                .map(_.toDTO)
                .provide(env)
            }
          }
        } ~ (path("media") & entity(parse[Base64Image]) & idToken) { (image, token) =>
          pathEnd {
            complete {
              MediaService
                .insert(token, image)
                .provide(env)
            }
          }
        }
      } ~ put {
        (path(Id[UserId]) & entity(parse[UserDTO]) & idToken) { (userId, user, idToken) =>
          pathEnd {
            complete {
              UserService
                .authenticate(idToken)
                .map(_.id)
                .someOrFail(UserNotFound)
                .filterOrFail(_ == userId)(AuthenticationError)
                .zipRight(UserService.update(userId, user))
                .provide(env)
            }
          }
        }
      }
    }
}
