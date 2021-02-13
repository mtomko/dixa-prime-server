package com.github.mtomko.dps.server

import cats.effect.{ExitCode, IO, IOApp}

object ServerApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    Server.resource[IO](Server.Config(9999)).use(_ => IO.never).as(ExitCode.Success)

}
