package com.github.mtomko.dps.server

import cats.effect.std.Dispatcher
import cats.effect.{Async, Resource}
import fs2.grpc.syntax.all._
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{ServerBuilder, ServerServiceDefinition, Server => GrpcServer}
import prime.PrimesServiceFs2Grpc

object Server {

  case class Config(port: Int)

  def resource[F[_]: Async](config: Config): Resource[F, GrpcServer] =
    Dispatcher[F].flatMap { dispatcher =>
      val primesService: ServerServiceDefinition =
        PrimesServiceFs2Grpc.bindService(dispatcher, new PrimesServiceImpl[F])

      ServerBuilder
        .forPort(config.port)
        .addService(primesService)
        .addService(ProtoReflectionService.newInstance()) // reflection makes lots of tooling happy
        .resource[F]
        .map(server => server.start)
    }

}
