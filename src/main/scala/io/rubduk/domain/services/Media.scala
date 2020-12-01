package io.rubduk.domain.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import io.rubduk.config.AppConfig.ImgurConfig
import io.rubduk.domain.Media
import io.rubduk.domain.errors.ThirdPartyError
import io.rubduk.infrastructure.models.{Base64Image, Link}
import zio.config.ZConfig
import zio.{Has, IO, URLayer, ZLayer}
import zio.macros.accessible

@accessible
object Media {

  trait Service {
    def uploadImage(image: Base64Image): IO[ThirdPartyError, Link]
  }


  val imgur: URLayer[ZConfig[ImgurConfig] with Has[ActorSystem], Media] = ZLayer.fromService { (config, system) =>
    new Service {
      implicit val sys = system

      val costam = Http().singleRequest()

      override def uploadImage(image: Base64Image): IO[ThirdPartyError, Link] = ???
    }
  }

}
