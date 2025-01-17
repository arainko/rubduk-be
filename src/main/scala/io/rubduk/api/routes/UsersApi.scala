package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as => parse}
import cats.syntax.functor._
import io.rubduk.api.serializers.unmarshallers.{limit, offset}
import io.rubduk.domain.errors.ApplicationError._

import io.rubduk.domain.models.auth._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.media._
import io.rubduk.domain.models.user._
import io.rubduk.api.directives._
import io.rubduk.application.{MediaService, UserService}
import io.rubduk.domain._
import io.rubduk.domain.typeclasses.BoolAlgebra._
import io.rubduk.domain.typeclasses.syntax.BoolAlgebraOps
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
  import io.rubduk.api.errors._

  override def routes: Route =
    pathPrefix("api" / "users") {
      get {
        parameters(
          "offset".as(offset) ? Offset(0),
          "limit".as(limit) ? Limit(10),
          "name".as[String].optional
        ) { (offset, limit, name) =>
          pathEnd {
            complete {
              val filters = name
                .map(UserFilter.NameContaining)
                .map(_.lift)
                .getOrElse(True)
              UserService
                .getAllPaginated(offset, limit, filters)
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
        } ~ (path("media") & entity(parse[ImageRequest]) & idToken) { (image, token) =>
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
        } ~ (path("me" / "pic") & idToken) { token =>
          entity(parse[MediumId]) { mediumId =>
            pathEnd {
              complete {
                UserService
                  .updateProfilePicture(token, mediumId)
                  .provide(env)
              }
            }
          }
        }
      } ~ (delete & idToken) { token =>
        path("me") {
          pathEnd {
            complete {
              UserService
                .delete(token)
                .provide(env)
            }
          }
        } ~ path("media" / Id[MediumId]) { mediumId =>
          pathEnd {
            complete {
              MediaService
                .delete(token, mediumId)
                .provide(env)
            }
          }
        }
      }
    }
}
