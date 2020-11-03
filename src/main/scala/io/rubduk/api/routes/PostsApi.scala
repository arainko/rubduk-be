package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as => parse}
import cats.syntax.functor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.api.custom.PlaceholderDirectives._
import io.rubduk.api.serializers.Unmarshallers.{limit, offset}
import io.rubduk.domain.services.PostService
import io.rubduk.domain.{PostRepository, UserRepository}
import io.rubduk.infrastructure.converters.IdConverter.idCodec
import io.rubduk.infrastructure.converters.IdConverter.Id
import zio.{Runtime => _}
import io.rubduk.infrastructure.models.{Limit, Offset, PostDTO, PostId, UserId}

object PostsApi {
  def apply(env: PostRepository with UserRepository): Route = new PostsApi(env).routes
}

class PostsApi(env: PostRepository with UserRepository) extends Api.Service {

  override def routes: Route =
    pathPrefix("api" / "posts") {
      get {
        parameters(
          "offset".as(offset) ? Offset(0),
          "limit".as(limit) ? Limit(10)
        ) { (offset, limit) =>
          pathEnd {
            complete {
              PostService
                .getAllPaginated(offset, limit)
                .map(_.map(_.toDTO))
                .provide(env)
            }
          }
        } ~ path(Id[PostId]) { postId =>
          pathEnd {
            complete {
              PostService
                .getById(postId)
                .map(_.toDTO)
                .provide(env)
            }
          }
        }
      } ~ post {
        (userId & entity(parse[PostDTO])) { (userId, post) =>
          complete {
            PostService
              .insert(userId, post)
              .provide(env)
          }
        }
      } ~ put {
        (path(Id[PostId]) & userId & entity(parse[PostDTO])) { (postId, userId, post) =>
          complete {
            PostService
              .update(postId, userId, post)
              .provide(env)
          }
        }
      }
    }
}