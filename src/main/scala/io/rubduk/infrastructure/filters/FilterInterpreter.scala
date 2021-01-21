package io.rubduk.infrastructure.filters

import io.rubduk.domain.models.comment.CommentFilter
import io.rubduk.domain.models.friendrequest.FriendRequestFilter
import io.rubduk.domain.models.media.MediaFilter
import io.rubduk.domain.models.post.PostFilter
import io.rubduk.domain.models.user.UserFilter
import io.rubduk.infrastructure.SlickPGProfile.api._
import io.rubduk.infrastructure.mappers._
import io.rubduk.infrastructure.tables._

trait FilterInterpreter[A, B] {
  def apply(filter: A): B
}

object FilterInterpreter {
  type SlickInterpreter[A, B] = FilterInterpreter[A, Filter[B]]

  def apply[A, B](implicit interpreter: FilterInterpreter[A, B]): FilterInterpreter[A, B] = interpreter

  implicit val postFilterInterpreter: SlickInterpreter[PostFilter, Posts.Schema] = {
    case PostFilter.ByUser(userId) => Filter(_.userId === userId)
  }

  implicit val mediaFilterInterpreter: SlickInterpreter[MediaFilter, Media.Schema] = {
    case MediaFilter.ByUser(userId) => Filter(_.userId === userId)
  }

  implicit val commentFilterInterpreter: SlickInterpreter[CommentFilter, Comments.Schema] =
    (_: CommentFilter) => Filter.productEmpty

  implicit val userFilterInterpreter: SlickInterpreter[UserFilter, Users.Schema] = {
    case UserFilter.ById(id) => Filter(_.id === id)
    case UserFilter.ByEmail(email) => Filter(_.email === email)
      case UserFilter.NameContaining(name) =>
        Filter { user =>
          val normalizedName = user.name.toLowerCase
          val normalizedSurname = user.lastName.toLowerCase.getOrElse("")
          (normalizedName ++ " " ++ normalizedSurname).like(s"%${name.toLowerCase}%") ||
          (normalizedSurname ++ " " ++ normalizedSurname).like(s"%${name.toLowerCase}%")
        }
    }

  implicit val friendRequestFilterInterpreter: SlickInterpreter[FriendRequestFilter, FriendRequests.Schema] = {
    case FriendRequestFilter.SentByUser(userId) => Filter(_.fromUserId === userId)
    case FriendRequestFilter.SentToUser(userId) => Filter(_.toUserId === userId)
    case FriendRequestFilter.WithStatus(status) => Filter(_.status === status)
  }

}
