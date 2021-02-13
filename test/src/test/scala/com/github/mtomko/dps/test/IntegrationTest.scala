package com.github.mtomko.dps.test

import cats.effect.IO
import com.github.mtomko.dps.client.Client
import com.github.mtomko.dps.proxy.Proxy
import com.github.mtomko.dps.server.Server
import fs2.Stream
import io.grpc.Metadata
import munit.CatsEffectSuite
import prime.PrimeRequest

class IntegrationTest extends CatsEffectSuite {

  test("everything works") {
    val s =
      for {
        _ <- Stream.resource(Proxy.resource[IO](Proxy.Config("127.0.0.1", 4444), 8888))
        _ <- Stream.resource(Server.resource[IO](Server.Config(4444)))
        c <- Stream.resource(Client.resource[IO]("127.0.0.1", 8888))
        primes <- Stream.eval(c.primes(PrimeRequest.of(10), new Metadata).map(_.next).compile.toList)
      } yield assertEquals(primes, List(2, 3, 5, 7))

    s.compile.drain
  }

}
