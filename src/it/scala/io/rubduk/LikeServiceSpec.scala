package io.rubduk

import io.rubduk.suites.End2EndTestSuite
import zio.test._
import zio.test.Assertion._
import io.rubduk.domain.repositories._
import io.rubduk.application.LikeService
import io.rubduk.domain.models.auth.IdToken
import io.rubduk.domain.models.user.UserRecord
import io.rubduk.domain.errors.ApplicationError.PostAlreadyLiked
import io.rubduk.domain.errors.ApplicationError.PostNotLiked

object LikeServiceSpec extends End2EndTestSuite {

  private val likePostSuite = suite("addLike should")(
    testM("add a like to a post") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId  <- PostRepository.insert(post)
        preLikeCount <- LikeRepository.count(postId)
        _ <- LikeRepository.addLike(postId, userId)
        count <- LikeRepository.count(postId)
      } yield assert(preLikeCount)(isZero) &&
        assert(count)(equalTo(1))
    },
    testM("should not allow liking the post twice") {
       val user = Entities.user.toDAO
       val userToken = tokenizeUser(user)

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId  <- PostRepository.insert(post)
        preLikeCount <- LikeRepository.count(postId)
        _ <- LikeService.likePost(userToken, postId)
        secondLikeResult <- LikeService.likePost(userToken, postId).either
        count <- LikeRepository.count(postId)
      } yield assert(preLikeCount)(isZero) &&
        assert(count)(equalTo(1)) &&
        assert(secondLikeResult)(isLeft(equalTo(PostAlreadyLiked)))
    }
  )

  private val unlikePostSuite = suite("unlike should")(
    testM("allow a user to like a post") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId  <- PostRepository.insert(post)
        preLikeCount <- LikeRepository.count(postId)
        _ <- LikeRepository.addLike(postId, userId)
        count <- LikeRepository.count(postId)
        _ <- LikeRepository.removeLike(postId, userId)
        postUnlikeCount <- LikeRepository.count(postId)
      } yield assert(preLikeCount)(isZero) &&
        assert(count)(equalTo(1)) &&
        assert(postUnlikeCount)(isZero)
    },
    testM("disallow a user from unliking a post he didn't like in the first place") {
       val user = Entities.user.toDAO
       val userToken = tokenizeUser(user)

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId  <- PostRepository.insert(post)
        count <- LikeRepository.count(postId)
        unlikeResult <- LikeService.unlikePost(userToken, postId).either
      } yield assert(count)(isZero) &&
        assert(unlikeResult)(isLeft(equalTo(PostNotLiked)))
    }
  )

  private def tokenizeUser(user: UserRecord) =
    IdToken(
      s"${user.email}:::${user.name}:::${user.lastName.getOrElse("LASTNAME")}"
    )

  def spec = suite("Like Service should")(
    likePostSuite,
    unlikePostSuite
  ).provideCustomLayer(End2EndTestSuite.testLayer.orDie)
}
