package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as => parse}
import cats.syntax.functor._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.api.custom.PlaceholderDirectives._
import io.rubduk.api.serializers.Unmarshallers.{limit, offset}
import io.rubduk.domain.services.CommentService
import io.rubduk.domain.{CommentRepository, PostRepository}
import io.rubduk.infrastructure.converters.IdConverter.idCodec
import io.rubduk.infrastructure.converters.IdConverter.Id
import zio.{Runtime => _}
import io.rubduk.infrastructure.models.{Limit, Offset, PostDTO, PostId, UserId}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._


object CommentsApi {
  def apply(env: CommentRepository with PostRepository): Route = new CommentsApi(env).routes
}

class CommentsApi(env: CommentRepository with PostRepository) extends Api.Service {

  override def routes: Route =
    pathPrefix("api" / "hello-comment") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
}
