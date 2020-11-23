import com.typesafe.sbt.packager.docker.{Cmd, CmdLike, ExecCmd}

val akkaHttpVersion   = "10.2.0"
val akkaVersion       = "2.6.9"
val slickVersion      = "3.3.3"
val zioVersion        = "1.0.1"
val zioLoggingVersion = "0.5.1"
val zioConfigVersion  = "1.0.0-RC27"
val circeVersion      = "0.12.3"

val TestItTest = "it, test"

val dockerReleaseSettings = Seq(
  dockerExposedPorts := Seq(8080),
  dockerExposedVolumes := Seq("/opt/docker/logs"),
  dockerBaseImage := "adoptopenjdk/openjdk12:x86_64-ubuntu-jre-12.0.2_10"
)

scalacOptions += "-Ymacro-annotations"

val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "io.rubduk",
        scalaVersion := "2.13.3"
      )
    ),
    name := "rubduk",
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"   %% "akka-http"                   % akkaHttpVersion,
      "de.heikoseeberger"   %% "akka-http-circe"             % "1.34.0",
      "com.typesafe.akka"   %% "akka-actor-typed"            % akkaVersion,
      "com.typesafe.akka"   %% "akka-stream"                 % akkaVersion,
      "com.typesafe.slick"  %% "slick"                       % slickVersion,
      "com.typesafe.slick"  %% "slick-hikaricp"              % slickVersion,
      "dev.zio"             %% "zio"                         % zioVersion,
      "dev.zio"             %% "zio-config"                  % zioConfigVersion,
      "dev.zio"             %% "zio-config-magnolia"         % zioConfigVersion,
      "dev.zio"             %% "zio-config-typesafe"         % zioConfigVersion,
      "dev.zio"             %% "zio-macros"                  % zioVersion,
      "io.scalac"           %% "zio-akka-http-interop"       % "0.4.0",
      "io.scalac"           %% "zio-slick-interop"           % "0.2.0",
      "dev.zio"             %% "zio-interop-reactivestreams" % "1.0.3.5",
      "ch.qos.logback"       % "logback-classic"             % "1.2.3",
      "dev.zio"             %% "zio-logging"                 % zioLoggingVersion,
      "dev.zio"             %% "zio-logging-slf4j"           % zioLoggingVersion,
      "org.postgresql"       % "postgresql"                  % "42.2.18",
      "com.github.tminglei" %% "slick-pg"                    % "0.19.3",
      "org.typelevel"       %% "cats-core"                   % "2.2.0",
      "io.scalaland"        %% "chimney"                     % "0.6.0",
      "io.circe"            %% "circe-core"                  % circeVersion,
      "io.circe"            %% "circe-generic"               % circeVersion,
      "io.circe"            %% "circe-parser"                % circeVersion,
      "io.circe"            %% "circe-generic-extras"        % "0.13.0",
      "com.google.apis"      % "google-api-services-oauth2"  % "v2-rev157-1.25.0",
      "com.typesafe.akka"   %% "akka-http-testkit"           % akkaHttpVersion % TestItTest,
      "com.typesafe.akka"   %% "akka-stream-testkit"         % akkaVersion     % TestItTest,
      "com.typesafe.akka"   %% "akka-actor-testkit-typed"    % akkaVersion     % TestItTest,
      "dev.zio"             %% "zio-test-sbt"                % zioVersion      % TestItTest
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    dockerReleaseSettings
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .enablePlugins(DockerPlugin, JavaAppPackaging, FlywayPlugin)
