package com.github.mtomko.dps.proxy

import cats.effect.{ConcurrentEffect, Resource, Sync}
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Server, ServerBuilder, ServerServiceDefinition}
import org.lyranthe.fs2_grpc.java_runtime.implicits._
//import org.lyranthe.fs2_grpc.java_runtime.implicits._ // dohj thinks this is unused
import prime.PrimesServiceFs2Grpc

object Proxy {

  final case class Config(targetIp: String, targetPort: Int)

  def resource[F[_]: Sync: ConcurrentEffect](config: Config, port: Int): Resource[F, Server] = {
    val client = new PrimesServiceProxyImpl[F](config)
    val primesService: ServerServiceDefinition = PrimesServiceFs2Grpc.bindService(client)

    ServerBuilder
      .forPort(port)
      .addService(primesService)
      .addService(ProtoReflectionService.newInstance()) // reflection makes lots of tooling happy
      .resource[F]
      .map(proxy => proxy.start)
  }

}
