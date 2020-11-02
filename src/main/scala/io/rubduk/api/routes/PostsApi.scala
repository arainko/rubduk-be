package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives
import cats.syntax.functor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.api.serializers.Unmarshallers.{limit, offset}
import io.rubduk.domain.services.PostService
import io.rubduk.domain.{PostRepository, UserRepository}
import io.rubduk.infrastructure.converters.IdConverter
import io.rubduk.infrastructure.converters.IdConverter.Id
import io.rubduk.infrastructure.models.{Limit, Offset, PostDTO, PostId, UserId}

import scala.util.Try

object PostsApi {
  def apply(env: PostRepository with UserRepository): Route = new PostsApi(env).routes
}

class PostsApi(env: PostRepository with UserRepository) extends Api.Service {

  private val __placeholderUserId__ = UserId(2) // TODO: add an auth directive

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
                .map(_.map(_.toDTO(__placeholderUserId__)))
                .provide(env)
            }
          }
        } ~ path(Id[PostId]) { postId =>
          pathEnd {
            complete {
              PostService
                .getById(postId)
                .map(_.toDTO(__placeholderUserId__))
                .provide(env)
            }
          }
        }
      } ~ post {
        entity(MarshallingDirectives.as[PostDTO]) { post =>
          complete {
            PostService
              .insert(__placeholderUserId__, post)
              .provide(env)
          }
        }
      } ~ put {
        (path(Id[PostId]) & entity(MarshallingDirectives.as[PostDTO])) { (postId, post) =>
          complete {
            PostService
              .update(postId, post)
              .provide(env)
          }
        }
      }
    }
}
