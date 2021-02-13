ThisBuild / scalaVersion := "2.13.4"

lazy val versions = new {
  val betterMonadicFor = "0.3.1"
  val cats = "2.4.1"
  val catsEffect = "2.3.1"
  val console4Cats = "0.8.1"
  val fs2 = "2.5.0"
  val fs2Grpc = "0.8.0"
  val grpc = "1.30.2"
  val munit = "0.7.21"
  val munitCatsEffect2 = "0.13.0"
  val protobuf = "3.14.0"
  val scalaPbLenses = "0.10.8"
}

lazy val libraries = new {
  val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % versions.betterMonadicFor
  val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect
  val catsCore = "org.typelevel" %% "cats-core" % versions.cats
  val console4Cats = "dev.profunktor" %% "console4cats" % versions.console4Cats
  val fs2 = "co.fs2" %% "fs2-core" % versions.fs2
  val fs2Grpc = "org.lyranthe.fs2-grpc" %% "java-runtime" % versions.fs2Grpc
  val grpcApi = "io.grpc" % "grpc-api" % versions.grpc
  val grpcNetty = "io.grpc" % "grpc-netty" % versions.grpc
  val grpcServices = "io.grpc" % "grpc-services" % versions.grpc
  val protobuf = "com.google.protobuf" % "protobuf-java" % versions.protobuf
  val scalaPbLenses = "com.thesamet.scalapb" %% "lenses" % versions.scalaPbLenses
  // test dependencies
  val munit = "org.scalameta" %% "munit" % versions.munit
  val munitScalaCheck = "org.scalameta" %% "munit-scalacheck" % versions.munit
  val munitCatsEffect2 = "org.typelevel" %% "munit-cats-effect-2" % versions.munitCatsEffect2
}

lazy val commonSettings = List(testFrameworks := List(new TestFramework("munit.Framework")))

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(organization := "com.github.mtomko", scalaVersion := "2.13.4", version := "1.0")),
    name := "dixa-prime-service",
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
      name := "dixa-prime-service-protobuf",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.fs2,
        libraries.grpcApi,
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
      name := "dixa-prime-service-client",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.console4Cats,
        libraries.fs2,
        libraries.fs2Grpc,
        libraries.grpcApi,
        libraries.grpcNetty % Runtime,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test
      ),
      addCompilerPlugin(libraries.betterMonadicFor)
    )
    .disablePlugins(RevolverPlugin)

lazy val proxy =
  project
    .in(file("proxy"))
    .dependsOn(protobuf, client)
    .settings(commonSettings: _*)
    .settings(
      name := "dixa-prime-service-proxy",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.fs2,
        libraries.fs2Grpc,
        libraries.grpcApi,
        libraries.grpcNetty % Runtime,
        libraries.grpcServices,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test
      ),
      addCompilerPlugin(libraries.betterMonadicFor)
    )

lazy val server =
  project
    .in(file("server"))
    .dependsOn(protobuf)
    .settings(commonSettings: _*)
    .settings(
      name := "dixa-prime-service-server",
      libraryDependencies ++= List(
        libraries.catsCore,
        libraries.catsEffect,
        libraries.fs2,
        libraries.fs2Grpc,
        libraries.grpcApi,
        libraries.grpcNetty % Runtime,
        libraries.grpcServices,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test
      ),
      addCompilerPlugin(libraries.betterMonadicFor)
    )

lazy val test =
  project
    .in(file("test"))
    .dependsOn(client, protobuf, proxy, server)
    .settings(commonSettings: _*)
    .settings(
      name := "dixa-prime-service-test",
      skip in publish := true,
      libraryDependencies ++= List(
        libraries.catsEffect % Test,
        libraries.fs2 % Test,
        libraries.fs2Grpc % Test,
        libraries.grpcApi % Test,
        libraries.grpcNetty % Runtime,
        libraries.grpcServices % Test,
        libraries.munit % Test,
        libraries.munitScalaCheck % Test,
        libraries.munitCatsEffect2 % Test
      ),
      addCompilerPlugin(libraries.betterMonadicFor)
    )
    .disablePlugins(AssemblyPlugin, RevolverPlugin)
