//package io.rubduk.api
//
//import io.rubduk.Entities
//import io.rubduk.api.routes.Api
//import io.rubduk.suites.End2EndTestSuite
//import zio.ZIO
//import zio.blocking.effectBlocking
//import zio.test._
//import cats.implicits._
//import zio.test.environment.TestEnvironment
//import io.circe.parser.decode
//import io.rubduk.domain.models.UserDTO
//import io.rubduk.domain.models.Page
//import akka.http.scaladsl.model.StatusCodes
//import zio.test.Assertion._
//import akka.http.scaladsl.model.HttpEntity
//import akka.http.scaladsl.model.ContentTypes
//import akka.util.ByteString
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
//import io.circe.syntax._
//import io.rubduk.application.UserService
//import io.rubduk.domain.models.UserId
//
//object UserEndToEndSpec extends End2EndTestSuite {
//  import io.rubduk.api.serializers.codecs._
//
//  private val userNotFoundMessage      = "Requested user was not found."
//  private val userAlreadyExistsMessage = "User with specified email already exists."
//
//  val paginationSuite = suite("api/users GET should")(
//    testM("return a page of Users") {
//      // given
//      val usersToInsert = (0 until 5).map(_ => Entities.user)
//
//      for {
//        route <- Api.routes
//        _ <- ZIO.foreachPar_(usersToInsert) { user =>
//          UserService.insert(user.toDTO)
//        }
//
//        // when
//        routeAssertions <- effectBlocking {
//          Get("/api/users?offset=0&limit=5") ~> route ~> check {
//            val decodedUserPage = decode[Page[UserDTO]](responseAs[String])
//
//            // then the status is OK and the fetched Page is of size 5
//            assert(status)(equalTo(StatusCodes.OK)) &&
//            assert(decodedUserPage.map(_.entities.size))(isRight(equalTo(5)))
//          }
//        }
//      } yield routeAssertions
//    }
//  )
//
//  val getByIdSuite = suite("api/users/{userId} GET should")(
//    testM("return an User with specified id") {
//      // given
//      val userToInsert = Entities.user
//
//      for {
//        route        <- Api.routes
//        insertedId   <- UserService.insert(userToInsert.toDTO)
//        expectedUser <- UserService.getById(insertedId).map(_.toDTO)
//
//        // when
//        routeAssertions <- effectBlocking {
//          Get(s"/api/users/${insertedId.value}") ~> route ~> check {
//
//            // then the status is OK and the fetched User matches the expected one
//            val decodedUser = decode[UserDTO](responseAs[String])
//            assert(status)(equalTo(StatusCodes.OK)) &&
//            assert(decodedUser)(isRight(equalTo(expectedUser)))
//          }
//        }
//      } yield routeAssertions
//    },
//    testM("fail with a 404 error when trying to fetch a non-existent User") {
//      // given
//      val nonExistentUserId = Long.MaxValue
//
//      for {
//        route <- Api.routes
//
//        // when
//        routeAssertions <- effectBlocking {
//          Get(s"/api/users/$nonExistentUserId") ~> route ~> check {
//
//            // then the status is NotFound and an explanatory message is returned
//            assert(status)(equalTo(StatusCodes.NotFound)) &&
//            assert(responseAs[String])(equalTo(userNotFoundMessage))
//          }
//        }
//      } yield routeAssertions
//    }
//  )
//
//  val insertSuite = suite("api/users POST should")(
//    testM("insert User") {
//      // given
//      val user     = Entities.user.toDTO
//      val userJson = user.asJson.toString
//
//      for {
//        route <- Api.routes
//
//        // when
//        (insertedId, routeAssertions) <- effectBlocking {
//          val requestEntity = HttpEntity(ContentTypes.`application/json`, ByteString(userJson))
//          Post("/api/users", requestEntity) ~> route ~> check {
//
//            // then the status is OK, the inserted id is returned and the User is inserted into the database
//            val insertedId = UserId(responseAs[String].toLong)
//            val assertion  = assert(status)(equalTo(StatusCodes.OK))
//            (insertedId, assertion)
//          }
//        }
//        fetchedUser <- UserService.getById(insertedId).map(_.toDTO)
//        expectedUser = user.copy(id = insertedId.some, createdOn = fetchedUser.createdOn)
//      } yield routeAssertions && assert(fetchedUser)(equalTo(expectedUser))
//    },
//    testM("fail with a 422 error when trying to insert a User with identical email ") {
//      // given
//      val user     = Entities.user
//      val userJson = user.toDTO.asJson.toString
//
//      for {
//        route <- Api.routes
//        _     <- UserService.insert(user.toDTO)
//
//        // when
//        routeAssertions <- effectBlocking {
//          val requestEntity = HttpEntity(ContentTypes.`application/json`, ByteString(userJson))
//          Post("/api/users", requestEntity) ~> route ~> check {
//
//            // then the status is UnprocessableEntity and an explanatory message is returned
//            assert(status)(equalTo(StatusCodes.UnprocessableEntity)) &&
//            assert(responseAs[String])(equalTo(userAlreadyExistsMessage))
//          }
//        }
//      } yield routeAssertions
//    }
//  )
//
//  private val updateSuite = suite("api/users/{userId} PUT should")(
//    testM("update User given its id") {
//      // given
//      val userToUpdate = Entities.user.toDTO
//      val updatedUserJson = userToUpdate
//        .copy(name = "UPDATED-NAME")
//        .asJson
//        .toString
//
//      for {
//        route      <- Api.routes
//        insertedId <- UserService.insert(userToUpdate)
//
//        // when
//        routeAssertions <- effectBlocking {
//          val requestEntity = HttpEntity(ContentTypes.`application/json`, ByteString(updatedUserJson))
//          Put(s"/api/users/${insertedId.value}", requestEntity) ~> route ~> check {
//
//            // then the status is OK and the user is updated
//            assert(status)(equalTo(StatusCodes.OK))
//          }
//        }
//        fetchedUser <- UserService.getById(insertedId).map(_.toDTO)
//        expectedUser = userToUpdate.copy(
//          id = insertedId.some,
//          name = "UPDATED-NAME",
//          createdOn = fetchedUser.createdOn
//        )
//      } yield routeAssertions && assert(expectedUser)(equalTo(fetchedUser))
//    },
//    testM("fail with a 404 error if trying to update a non-existent User") {
//      // given
//      val nonExistentUserId = Long.MaxValue
//      val userToUpdate      = Entities.user.toDTO
//      val updatedUserJson = userToUpdate
//        .copy(name = "UPDATED-NAME")
//        .asJson
//        .toString
//
//      for {
//        route <- Api.routes
//
//        // when
//        routeAssertions <- effectBlocking {
//          val requestEntity = HttpEntity(ContentTypes.`application/json`, ByteString(updatedUserJson))
//          Put(s"/api/users/${nonExistentUserId}", requestEntity) ~> route ~> check {
//
//            // then the status is NotFound and an explanatory message is returned
//            assert(status)(equalTo(StatusCodes.NotFound)) &&
//            assert(responseAs[String])(equalTo(userNotFoundMessage))
//          }
//        }
//      } yield routeAssertions
//    },
//    testM("fail with a 422 error if trying to update an User's email to an already existing one") {
//      // given
//      val userToInsert = Entities.user.toDTO
//      val userToUpdate = Entities.user.toDTO
//      val updatedUserJson = userToUpdate
//        .copy(email = userToInsert.email)
//        .asJson
//        .toString
//
//      for {
//        route      <- Api.routes
//        _          <- UserService.insert(userToInsert)
//        insertedId <- UserService.insert(userToUpdate)
//
//        // when
//        routeAssertions <- effectBlocking {
//          val requestEntity = HttpEntity(ContentTypes.`application/json`, ByteString(updatedUserJson))
//          Put(s"/api/users/${insertedId.value}", requestEntity) ~> route ~> check {
//
//            // then the status is UnprocessableEntity and the user isn't
//            // updated and an explanatory message is returned
//            assert(status)(equalTo(StatusCodes.UnprocessableEntity)) &&
//            assert(responseAs[String])(equalTo(userAlreadyExistsMessage))
//          }
//        }
//      } yield routeAssertions
//    }
//  )
//
//  override def spec: ZSpec[TestEnvironment, Any] =
//    suite("Users End-To-End Spec")(
//      paginationSuite,
//      getByIdSuite,
//      insertSuite,
//      updateSuite
//    ).provideCustomLayer(End2EndTestSuite.testLayer)
//}
