package com.github.mtomko.dps.client

import cats.effect.std.Dispatcher
import cats.effect.{Async, Resource, Sync}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, Metadata}
import org.lyranthe.fs2_grpc.java_runtime.implicits._
// import org.lyranthe.fs2_grpc.java_runtime.implicits._ // dohj thinks this is unused
import prime.PrimesServiceFs2Grpc

object Client {

  def managedChannelResource[F[_]: Sync](ip: String, port: Int): Resource[F, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress(ip, port)
      .usePlaintext()
      .resource[F]

  def resource[F[_]: Async](ip: String, port: Int): Resource[F, PrimesServiceFs2Grpc[F, Metadata]] =
    managedChannelResource(ip, port).flatMap(mc =>
      Dispatcher[F].map { dispatcher =>
        PrimesServiceFs2Grpc.stub[F](dispatcher, mc)
      }
    )

}
