package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.post
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as => parse}
import cats.syntax.functor._
import io.rubduk.api.serializers.unmarshallers._
import io.rubduk.domain._
import io.rubduk.domain.errors.UserError.UserNotFound
import io.rubduk.domain.models.{post => post_}
import post_._
import io.rubduk.domain.models.comment._
import io.rubduk.domain.models.common._
import io.rubduk.domain.models.user._
import io.rubduk.api.directives._
import io.rubduk.application.{CommentService, PostService, UserService}

object PostsApi {

  def apply(env: PostRepository with UserRepository with CommentRepository with TokenValidation): Route =
    new PostsApi(env).routes
}

class PostsApi(env: PostRepository with UserRepository with CommentRepository with TokenValidation)
    extends Api.Service {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.rubduk.api.serializers.codecs._
  import io.rubduk.domain.errors._

  override def routes: Route =
    pathPrefix("api" / "posts") {
      get {
        parameters(
          "offset".as(offset) ? Offset(0),
          "limit".as(limit) ? Limit(10),
          "userId".as(IdParam[UserId]).optional
        ) { (offset, limit, maybeUserId) =>
          pathEnd {
            complete {
              val filters = Seq(maybeUserId.map(PostFilter.ByUser)).flatten
              PostService
                .getAllPaginated(offset, limit, filters: _*)
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
          parameters(
            "offset".as(offset) ? Offset(0),
            "limit".as(limit) ? Limit(50)
          ) { (offset, limit) =>
            pathEnd {
              complete {
                CommentService
                  .getByPostIdPaginated(postId, offset, limit)
                  .map(_.map(_.toDTO))
                  .provide(env)
              }
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
      } ~ (post & idToken) { idToken =>
        entity(parse[PostDTO]) { post =>
          pathEnd {
            complete {
              UserService
                .authenticate(idToken)
                .map(_.id)
                .someOrFail(UserNotFound)
                .flatMap(userId => PostService.insert(userId, post))
                .provide(env)
            }
          }
        } ~ path(Id[PostId] / "comments") { postId =>
          entity(parse[CommentDTO]) { comment =>
            complete {
              UserService
                .authenticate(idToken)
                .map(_.id)
                .someOrFail(UserNotFound)
                .flatMap(userId => CommentService.insert(postId, userId, comment))
                .provide(env)
            }
          }
        }
      } ~ (put & idToken) { idToken =>
        (path(Id[PostId]) & entity(parse[PostDTO])) { (postId, post) =>
          pathEnd {
            complete {
              UserService
                .authenticate(idToken)
                .map(_.id)
                .someOrFail(UserNotFound)
                .flatMap(userId => PostService.update(postId, userId, post))
                .provide(env)
            }
          }
        } ~ path(Id[PostId] / "comments" / Id[CommentId]) { (postId, commentId) =>
          entity(parse[CommentDTO]) { comment =>
            complete {
              UserService
                .authenticate(idToken)
                .map(_.id)
                .someOrFail(UserNotFound)
                .flatMap(userId => CommentService.update(userId, postId, commentId, comment))
                .provide(env)
            }
          }
        }
      }
    }
}
