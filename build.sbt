ThisBuild / scalaVersion := "3.0.0-RC2"

lazy val versions = new {
  val betterMonadicFor = "0.3.1"
  val cats = "2.5.0"
  val catsEffect = "3.0.1"
  val fs2 = "3.0.1"
  val fs2Grpc = "1.0.0"
  val grpc = "1.36.1"
  val http4s = "1.0.0-M20"
  val logback = "1.2.3"
  val munit = "0.7.23"
  val munitCatsEffect3 = "1.0.1"
  val protobuf = "3.15.6"
  val refined = "0.9.22"
  val scalaPbLenses = "0.11.0"
  val scalaCheckEffect = "1.0.0"
  val shapeless = "2.3.3"
}

lazy val libraries = new {
  val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect
  val catsEffectStd = "org.typelevel" %% "cats-effect-std" % versions.catsEffect
  val catsCore = "org.typelevel" %% "cats-core" % versions.cats
  val fs2 = "co.fs2" %% "fs2-core" % versions.fs2
  val fs2Grpc = "org.lyranthe.fs2-grpc" %% "java-runtime" % versions.fs2Grpc
  val grpcApi = "io.grpc" % "grpc-api" % versions.grpc
  val grpcNetty = "io.grpc" % "grpc-netty" % versions.grpc
  val grpcServices = "io.grpc" % "grpc-services" % versions.grpc
  val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % versions.http4s
  val http4sClient = "org.http4s" %% "http4s-blaze-client" % versions.http4s
  val http4sCore = "org.http4s" %% "http4s-core" % versions.http4s
  val http4sDsl = "org.http4s" %% "http4s-dsl" % versions.http4s
  val http4sServer = "org.http4s" %% "http4s-server" % versions.http4s
  val logback = "ch.qos.logback" % "logback-classic" % versions.logback
  val protobuf = "com.google.protobuf" % "protobuf-java" % versions.protobuf
  val refined = "eu.timepit" %% "refined" % versions.refined
  val scalaPbLenses = "com.thesamet.scalapb" %% "lenses" % versions.scalaPbLenses
  val shapeless = "com.chuusai" %% "shapeless" % versions.shapeless

  // test dependencies
  val munit = "org.scalameta" %% "munit" % versions.munit
  val munitScalaCheck = "org.scalameta" %% "munit-scalacheck" % versions.munit
  val munitCatsEffect3 = "org.typelevel" %% "munit-cats-effect-3" % versions.munitCatsEffect3
  val refinedScalaCheck = "eu.timepit" %% "refined-scalacheck" % versions.refined
  val scalaCheckEffect = "org.typelevel" %% "scalacheck-effect-munit" % versions.scalaCheckEffect
}

lazy val commonSettings = List(testFrameworks := List(new TestFramework("munit.Framework")))

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(organization := "com.github.mtomko", scalaVersion := "2.13.4", version := "1.0")),
    name := "prime-service",
    skip in publish := true
  )
  .aggregate(client, protobuf, proxy, server, test)
  .disablePlugins(AssemblyPlugin, RevolverPlugin)

lazy val protobuf =
  project
    .in(file("protobuf"))
    .enablePlugins(Fs2Grpc)
    .settings(commonSettings: _*)
    .settings(
      name := "prime-service-protobuf",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.fs2,
        libraries.protobuf,
        libraries.scalaPbLenses,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test
      ),
      scalacOptions := List()
    )
    .disablePlugins(AssemblyPlugin, RevolverPlugin)

lazy val client =
  project
    .in(file("client"))
    .dependsOn(protobuf)
    .settings(commonSettings: _*)
    .settings(
      name := "prime-service-client",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.fs2,
        libraries.fs2Grpc,
        libraries.grpcApi,
        libraries.grpcNetty % Runtime,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test
      ),
    )
    .disablePlugins(RevolverPlugin)

lazy val proxy =
  project
    .in(file("proxy"))
    .dependsOn(protobuf, client)
    .settings(commonSettings: _*)
    .settings(
      name := "prime-service-proxy",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.fs2,
        libraries.grpcApi,
        libraries.grpcNetty % Runtime,
        libraries.http4sBlazeServer,
        libraries.http4sCore,
        libraries.http4sDsl,
        libraries.http4sServer,
        libraries.logback % Runtime,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test
      ),
    )

lazy val server =
  project
    .in(file("server"))
    .dependsOn(protobuf)
    .settings(commonSettings: _*)
    .settings(
      name := "prime-service-server",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.catsEffectStd,
        libraries.fs2,
        libraries.fs2Grpc,
        libraries.grpcApi,
        libraries.grpcNetty % Runtime,
        libraries.grpcServices,
        libraries.refined,
        libraries.shapeless,
        libraries.munit % Test,
        libraries.munitCatsEffect3 % Test,
        libraries.munitScalaCheck % Test,
        libraries.refinedScalaCheck % Test
      ),
    )

lazy val test =
  project
    .in(file("test"))
    .dependsOn(client, protobuf, proxy, server)
    .settings(commonSettings: _*)
    .settings(
      name := "prime-service-test",
      skip in publish := true,
      libraryDependencies ++= List(
        libraries.catsEffect % Test,
        libraries.fs2 % Test,
        libraries.fs2Grpc % Test,
        libraries.grpcApi % Test,
        libraries.grpcNetty % Runtime,
        libraries.grpcServices % Test,
        libraries.http4sClient % Test,
        libraries.logback % Runtime,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test,
        libraries.munitCatsEffect3 % Test,
        libraries.scalaCheckEffect % Test
      )
    )
    .disablePlugins(AssemblyPlugin, RevolverPlugin)
