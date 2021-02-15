package com.github.mtomko.dps.client

import cats.effect.Console.io._
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.grpc.Metadata
import prime.PrimeRequest

object ClientApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Blocker[IO]
      .use { blocker =>
        Client.resource[IO]("127.0.0.1", 9999).use { client =>
          putStr("Enter max: ") *>
            readLn
              .map(_.toInt)
              .flatMap { max =>
                client
                  .primes(PrimeRequest.of(max), new Metadata)
                  .evalMap(p => blocker.blockOn(putStrLn(p.next)))
                  .compile
                  .drain
              }
        }
      }
      .as(ExitCode.Success)

}
