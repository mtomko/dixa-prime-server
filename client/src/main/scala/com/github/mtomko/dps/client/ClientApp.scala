package com.github.mtomko.dps.client

import cats.effect.{ExitCode, IO, IOApp}
import io.grpc.Metadata
import prime.PrimeRequest

object ClientApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Client
      .resource[IO]("127.0.0.1", 9999)
      .use { client =>
        IO.print("Enter max: ") *>
          IO.readLine
            .map(_.toInt)
            .flatMap { max =>
              client
                .primes(PrimeRequest.of(max), new Metadata)
                .evalMap(p => IO.println(p.next))
                .compile
                .drain
            }
      }
      .as(ExitCode.Success)

}
