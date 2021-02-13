package com.github.mtomko.dps.client

import cats.effect.{ConcurrentEffect, Resource, Sync}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, Metadata}
import org.lyranthe.fs2_grpc.java_runtime.implicits._
// import org.lyranthe.fs2_grpc.java_runtime.implicits._ // dohj thinks this is unused
import prime.PrimesServiceFs2Grpc

object Client {

  def managedChannelResource[F[_]: ConcurrentEffect](ip: String, port: Int): Resource[F, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress(ip, port)
      .usePlaintext()
      .resource[F]

  def resource[F[_]: Sync: ConcurrentEffect](ip: String, port: Int): Resource[F, PrimesServiceFs2Grpc[F, Metadata]] =
    managedChannelResource(ip, port).map(mc => PrimesServiceFs2Grpc.stub[F](mc))

}
