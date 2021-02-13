package com.github.mtomko.dps.client

import cats.effect.Console.io._
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.grpc.Metadata
import prime.PrimeRequest

object ClientApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Blocker[IO]
      .use { blocker =>
        Client.resource[IO]("127.0.0.1", 9998).use { client =>
          client
            .primes(PrimeRequest.of(-5), new Metadata)
            .evalMap(p => blocker.blockOn(putStrLn(p.next)))
            .compile
            .drain
        }
      }
      .as(ExitCode.Success)

}
