package com.github.mtomko.dps.server

import cats.effect.{ConcurrentEffect, Resource}
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{ServerBuilder, ServerServiceDefinition, Server => GrpcServer}
import org.lyranthe.fs2_grpc.java_runtime.implicits._
//import org.lyranthe.fs2_grpc.java_runtime.implicits._ // dohj thinks this is unused
import prime.PrimesServiceFs2Grpc

object Server {

  case class Config(port: Int)

  def resource[F[_]: ConcurrentEffect](config: Config): Resource[F, GrpcServer] = {
    val primesService: ServerServiceDefinition = PrimesServiceFs2Grpc.bindService(new PrimesServiceImpl[F])

    ServerBuilder
      .forPort(config.port)
      .addService(primesService)
      .addService(ProtoReflectionService.newInstance()) // reflection makes lots of tooling happy
      .resource[F]
      .map(server => server.start)
  }

}
