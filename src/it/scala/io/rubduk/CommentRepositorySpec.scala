package io.rubduk

import io.rubduk.suites.End2EndTestSuite
import zio._
import zio.test._
import zio.test.Assertion._
import io.rubduk.domain.repositories._
import cats.syntax.option._
import io.rubduk.domain.models.common._
import io.rubduk.domain.typeclasses.BoolAlgebra.True
import io.rubduk.domain.models.comment.CommentId

object CommentRepositorySpec extends End2EndTestSuite {

  private val insertSuite = suite("insert should")(
    testM("insert the given comment") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId <- PostRepository.insert(post)
        comment = Entities.comment(postId, user.toDomain(None)).toDAO(userId)
        commentId <- CommentRepository.insert(postId, comment)
        fetched   <- CommentRepository.getById(commentId)
        expected = comment.copy(id = commentId.some)
      } yield assert(fetched)(isSome(equalTo(expected)))
    }
  )

  private val getByIdSuite = suite("getById should")(
    testM("not fetch a nonexisting comment") {
      val nonExistentCommentId = CommentId(Long.MaxValue)

      for {
        fetched <- CommentRepository.getById(nonExistentCommentId)
      } yield assert(fetched)(isNone)
    }
  )

  private val getAllSuite = suite("getAllShould")(
    testM("return a page of comments from a given post") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId <- PostRepository.insert(post)
        comments = (0 until 5).map(_ => Entities.comment(postId, user.toDomain(None)).toDAO(userId))
        _ <- ZIO.foreach_(comments)(com => CommentRepository.insert(postId, com))
        fetched <- CommentRepository.getByPostId(postId, Offset(0), Limit(5), True)
      } yield assert(fetched.size)(equalTo(5))
    }
  )

  private val updateSuite = suite("update should")(
    testM("update the given comment") {
      val user = Entities.user.toDAO

      for {
        userId <- UserRepository.insert(user)
        post = Entities.post.toDAO(userId)
        postId <- PostRepository.insert(post)
        comment = Entities.comment(postId, user.toDomain(None)).toDAO(userId)
        commentId <- CommentRepository.insert(postId, comment)
        _ <- CommentRepository.update(commentId, "UPDATED")
        fetched   <- CommentRepository.getById(commentId)
      } yield assert(fetched.map(_.contents))(isSome(equalTo("UPDATED")))
    }
  )

  def spec =
    suite("Comment Repository should")(
      insertSuite,
      getByIdSuite,
      getAllSuite,
      updateSuite
    ).provideCustomLayer(End2EndTestSuite.testLayer.orDie)

}
