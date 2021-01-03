//package io.rubduk.suites
//
//import akka.http.scaladsl.server.ExceptionHandler
//import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
//import com.typesafe.config.ConfigFactory
//import io.rubduk.api.Api
//import io.rubduk.api.routes.Api
//import io.rubduk.config.AppConfig
//import io.rubduk.domain.repositories.{CommentRepository, PostRepository, UserRepository}
//import io.rubduk.domain.{CommentRepository, PostRepository, UserRepository}
//import slick.interop.zio.DatabaseProvider
//import slick.jdbc.PostgresProfile
//import zio.config.typesafe.TypesafeConfig
//import zio.test.DefaultRunnableSpec
//import zio.{Has, ULayer, ZIO, ZLayer}
//
//trait End2EndTestSuite extends DefaultRunnableSpec with TestFrameworkInterface with RouteTest {
//  def failTest(msg: String): Nothing = throw new Exception(msg)
//
//  def testExceptionHandler: ExceptionHandler =
//    ExceptionHandler {
//      case e: Throwable => throw e
//    }
//}
//
//object End2EndTestSuite {
//
//  private val rawConfig      = ConfigFactory.load.resolve
//  private val configLayer    = TypesafeConfig.fromTypesafeConfig(rawConfig, AppConfig.descriptor)
//  private val dbConfigLayer  = ZIO.effect(rawConfig.getConfig("db")).toLayer
//  private val dbBackendLayer = ZLayer.succeed(PostgresProfile.backend)
//  private val apiConfigLayer = configLayer.map(c => Has(c.get.api))
//
//  private val repositoryLayer: ULayer[PostRepository with UserRepository with CommentRepository] = {
//    dbConfigLayer ++ dbBackendLayer >>>
//      DatabaseProvider.live >>>
//      PostRepository.live ++ UserRepository.live ++ CommentRepository.live
//  }.orDie
//
//  private val apiLayer: ULayer[Api] = (apiConfigLayer ++ repositoryLayer >>> Api.live).orDie
//
//  val testLayer: ULayer[PostRepository with UserRepository with CommentRepository with Api] =
//    repositoryLayer ++ apiLayer
//}
