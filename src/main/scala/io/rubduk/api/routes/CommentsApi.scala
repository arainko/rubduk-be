package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.rubduk.domain.{CommentRepository, PostRepository}
import zio.{Runtime => _}
import akka.http.scaladsl.model._


object CommentsApi {
  def apply(env: CommentRepository with PostRepository): Route = new CommentsApi(env).routes
}

class CommentsApi(env: CommentRepository with PostRepository) extends Api.Service {

  override def routes: Route =
    pathPrefix("api" / "comments") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Comments API</h1>"))
      }
    }
}