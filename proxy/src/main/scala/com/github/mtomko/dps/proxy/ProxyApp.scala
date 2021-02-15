package com.github.mtomko.dps.proxy

import cats.effect.{ExitCode, IO, IOApp}

object ProxyApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Proxy.resource[IO](Proxy.Config("127.0.0.1", 9999), 9998).use { _ =>
      IO.never.as(ExitCode.Success)
    }

}
