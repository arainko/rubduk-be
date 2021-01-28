package io.rubduk

import io.rubduk.suites.End2EndTestSuite

import zio._
import zio.test._
import zio.test.Assertion._
import io.rubduk.domain.repositories._
import io.rubduk.domain.models.post._
import cats.syntax.option._
import io.rubduk.domain.models.common._
import io.rubduk.domain.typeclasses.BoolAlgebra.True

object PostRepositorySpec extends End2EndTestSuite {

  private val insertSuite = suite("insert should")(
    testM("insert the given post") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId  <- PostRepository.insert(post)
        fetched <- PostRepository.getById(postId)
        expectedPost = post.copy(id = postId.some)
      } yield assert(fetched)(isSome(equalTo(expectedPost)))
    }
  )

  private val getByidSuite = testM("getById should not fetch a non-existent post") {
    val nonExistentPostId = PostId(Long.MaxValue)

    for {
      fetched <- PostRepository.getById(nonExistentPostId)
    } yield assert(fetched)(isNone)
  }

  private val getAllSuite = suite("getAll should")(
    testM("fetch a page of posts") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        posts = (0 until 5).map(_ => Entities.post.toDAO(userId))
        _       <- ZIO.foreach_(posts)(PostRepository.insert)
        fetched <- PostRepository.getAll(Offset(0), Limit(5), True)
      } yield assert(fetched.size)(equalTo(5))
    }
  )

  private val updateSuite = suite("update should")(
    testM("update the given post") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        updatedPost = post.copy(contents = "UPDATED")
        postId  <- PostRepository.insert(post)
        preUpdate <- PostRepository.getById(postId)
        _ <- PostRepository.update(postId, "UPDATED")
        postUpdate <- PostRepository.getById(postId)
      } yield assert(preUpdate.map(_.contents))(isSome(equalTo(post.contents))) &&
        assert(postUpdate.map(_.contents))(isSome(equalTo(updatedPost.contents)))
    }
  )

  def spec =
    suite("Post Repository:")(
      insertSuite,
      getAllSuite,
      updateSuite,
      getByidSuite
    ).provideCustomLayer(End2EndTestSuite.testLayer.orDie)

}
