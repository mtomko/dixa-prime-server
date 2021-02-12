package com.github.mtomko.dps.server

import cats.effect.{ExitCode, IO, IOApp, Sync, Timer}
import fs2.Stream
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Metadata, ServerBuilder, ServerServiceDefinition}
import prime.{PrimeRequest, PrimeResponse, PrimesServiceFs2Grpc}
import org.lyranthe.fs2_grpc.java_runtime.implicits._
//import org.lyranthe.fs2_grpc.java_runtime.implicits._ // dohj thinks this is unused
import scala.concurrent.duration._

class PrimesServiceImpl[F[_]: Sync: Timer] extends PrimesServiceFs2Grpc[F, Metadata] {
  override def primes(request: PrimeRequest, ctx: Metadata): Stream[F, PrimeResponse] =
    Stream.iterate(0)(_ + 1).takeWhile(_ < request.upTo).covary[F].metered(20.millis).map(PrimeResponse.of)
}

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val primesService: ServerServiceDefinition = PrimesServiceFs2Grpc.bindService(new PrimesServiceImpl[IO])

    ServerBuilder
      .forPort(9999)
      .addService(primesService)
      .addService(ProtoReflectionService.newInstance()) // reflection makes lots of tooling happy
      .stream[IO] // or for any F: Sync
      .evalMap(server => IO(server.start())) // start server
      .evalMap(_ => IO.never)
      .compile
      .drain
      .as(ExitCode.Success)
    //.evalMap(_ => IO.never) // server now running
  }

}
