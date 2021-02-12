package com.github.mtomko.dps.client

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.effect.Console.io._
import fs2.Stream
import io.grpc.{ManagedChannel, ManagedChannelBuilder, Metadata}
import org.lyranthe.fs2_grpc.java_runtime.implicits._
import prime.{PrimeRequest, PrimesServiceFs2Grpc}

object Client extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val managedChannelStream: Stream[IO, ManagedChannel] =
      ManagedChannelBuilder
        .forAddress("127.0.0.1", 9999)
        .usePlaintext()
        .stream[IO]

    def program(blocker: Blocker, stub: PrimesServiceFs2Grpc[IO, Metadata]): IO[Unit] =
      stub
        .primes(PrimeRequest.of(10), new Metadata())
        .evalMap(p => blocker.blockOn(putStrLn(p.next)))
        .compile
        .drain

    val io =
      for {
        blocker <- Stream.resource(Blocker[IO])
        managedChannel <- managedChannelStream
        client = PrimesServiceFs2Grpc.stub[IO](managedChannel)
        _ <- Stream.eval(program(blocker, client))
      } yield ()

    io.compile.drain.as(ExitCode.Success)
  }

}
