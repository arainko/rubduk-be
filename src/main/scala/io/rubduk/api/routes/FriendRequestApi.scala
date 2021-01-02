package io.rubduk.api.routes

import akka.http.scaladsl.server.Directives.{as => parse, _}
import akka.http.scaladsl.server.Route
import io.rubduk.api.directives._
import io.rubduk.application.FriendRequestAppService
import io.rubduk.domain._
import io.rubduk.domain.models.friendrequest.{FriendRequestFilterAggregate, FriendRequestId, FriendRequestRequest}
import io.rubduk.domain.repositories.FriendRequestRepository
import io.rubduk.domain.services.FriendRequestService
import io.circe.generic.auto._
import io.rubduk.domain.models.common.{Limit, Offset}
import zio.Has
import zio.clock._

class FriendRequestApi(
  env: Has[FriendRequestRepository.Service]
    with Clock
    with Has[FriendRequestService.Service]
    with UserRepository
    with TokenValidation
) extends Api.Service {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.rubduk.api.serializers.codecs._

  override def routes: Route =
    pathPrefix("api" / "friends") {
      (post & idToken & entity(parse[FriendRequestRequest])) { (token, request) =>
        pathEnd {
          complete {
            FriendRequestAppService
              .insert(request, token)
              .provide(env)
          }
        }
      } ~ (get & idToken) { token =>
        pathEnd {
          complete {
            FriendRequestAppService
              .getFriends(token, Offset(0), Limit(100), FriendRequestFilterAggregate())
              .provide(env)
          }
        }
      } ~ path("pending") {
        (get & idToken) { token =>
          pathEnd {
            complete {
              FriendRequestAppService
                .getPending(token, Offset(0), Limit(100), FriendRequestFilterAggregate())
                .provide(env)
            }
          }
        }
      } ~ path("accept" / Id[FriendRequestId]) { id =>
        (post & idToken) { token =>
          pathEnd {
            complete {
              FriendRequestAppService
                .acceptRequest(id, token)
                .provide(env)
            }
          }
        }
      }  ~ path("reject" / Id[FriendRequestId]) { id =>
        (post & idToken) { token =>
          pathEnd {
            complete {
              FriendRequestAppService
                .rejectRequest(id, token)
                .provide(env)
            }
          }
        }
      }
    }
}
