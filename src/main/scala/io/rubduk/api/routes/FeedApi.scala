package io.rubduk.api.routes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import io.rubduk.api.directives._
import io.rubduk.application.FriendRequestAppService
import io.rubduk.domain.{PostRepository, TokenValidation, UserRepository}
import io.rubduk.domain.models.common._
import io.rubduk.domain.services.{FriendRequestService, MediaReadService}
import zio.Has

class FeedApi(env: PostRepository with UserRepository with Has[FriendRequestService.Service] with Has[MediaReadService.Service] with TokenValidation) extends Api.Service {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.rubduk.api.serializers.codecs._
  import io.rubduk.api.serializers.unmarshallers._
  import io.rubduk.api.errors._

  override def routes: Route = pathPrefix("api" / "feed") {
    path("posts") {
      (get & idToken) { token =>
        parameters(
          "offset".as(offset) ? Offset(0),
          "limit".as(limit) ? Limit(50),
        ) { (offset, limit) =>
          pathEnd {
            complete {
              FriendRequestAppService
                .getFriendPostFeed(token, offset, limit)
                .provide(env)
            }
          }
        }
      }
    } ~ path("media") {
      (get & idToken) { token =>
        parameters(
          "offset".as(offset) ? Offset(0),
          "limit".as(limit) ? Limit(50),
        ) { (offset, limit) =>
          pathEnd {
            complete {
              FriendRequestAppService
                .getFriendMediaFeed(token, offset, limit)
                .provide(env)
            }
          }
        }
      }
    }
  }
}


