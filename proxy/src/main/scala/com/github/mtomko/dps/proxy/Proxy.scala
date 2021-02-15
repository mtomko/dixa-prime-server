package com.github.mtomko.dps.proxy

import cats.effect.{ConcurrentEffect, Resource, Timer}
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object Proxy {

  final case class Config(targetIp: String, targetPort: Int)

  def resource[F[_]: ConcurrentEffect](config: Proxy.Config, port: Int)(implicit
    T: Timer[F]
  ): Resource[F, Server[F]] = {
    val client = new PrimesServiceProxyImpl[F](config)
    val httpApp = ProxyRoutes.primeRoutes[F](client).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
    BlazeServerBuilder[F](global)
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .resource
  }
}
