package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{ as => parse }
import cats.syntax.functor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.api.custom.PlaceholderDirectives._
import io.rubduk.api.serializers.Unmarshallers.{ limit, offset }
import io.rubduk.domain.services.PostService
import io.rubduk.domain.services.CommentService
import io.rubduk.domain.{ CommentRepository, PostRepository, UserRepository }
import io.rubduk.infrastructure.converters.IdConverter.idCodec
import io.rubduk.infrastructure.converters.IdConverter.Id
import zio.{ Runtime => _ }
import io.rubduk.infrastructure.models.{ CommentDTO, CommentId, Limit, Offset, PostDTO, PostId }

object PostsApi {
  def apply(env: PostRepository with UserRepository with CommentRepository): Route = new PostsApi(env).routes
}

class PostsApi(env: PostRepository with UserRepository with CommentRepository) extends Api.Service {

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
        } ~ path(Id[PostId] / "comments") { postId =>
          pathEnd {
            complete {
              CommentService
                .getByPostId(postId, Offset(0), Limit(10))
                .map(_.map(_.toDTO))
                .provide(env)
            }
          }
        } ~ path(Id[PostId] / "comments" / Id[CommentId]) { (postId, commentId) =>
          pathEnd {
            complete {
              CommentService
                .getById(postId, commentId)
                .map(_.toDTO)
                .provide(env)
            }
          }
        }
      } ~ (post & userId) { userId =>
        entity(parse[PostDTO]) { post =>
          pathEnd {
            complete {
              PostService
                .insert(userId, post)
                .provide(env)
            }
          }
        } ~ path(Id[PostId] / "comments") { postId =>
          entity(parse[CommentDTO]) { comment =>
            complete {
              CommentService
                .insert(postId, userId, comment)
                .provide(env)
            }
          }
        }
      } ~ (put & userId) { userId =>
        (path(Id[PostId]) & entity(parse[PostDTO])) { (postId, post) =>
          pathEnd {
            complete {
              PostService
                .update(postId, userId, post)
                .provide(env)
            }
          }
        } ~ path(Id[PostId] / "comments" / Id[CommentId]) { (postId, commentId) =>
          entity(parse[CommentDTO]) { comment =>
            complete {
              CommentService
                .update(userId, postId, commentId, comment)
                .provide(env)
            }
          }
        }
      }
    }
}
