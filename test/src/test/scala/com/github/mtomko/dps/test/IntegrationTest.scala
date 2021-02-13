package com.github.mtomko.dps.test

import cats.effect.{IO, Resource}
import com.github.mtomko.dps.client.Client
import com.github.mtomko.dps.proxy.Proxy
import com.github.mtomko.dps.server.Server
import fs2.Stream
import io.grpc.{Metadata, StatusRuntimeException}
import munit.CatsEffectSuite
import prime.{PrimeRequest, PrimesServiceFs2Grpc}

class IntegrationTest extends CatsEffectSuite {

  val client: Resource[IO, PrimesServiceFs2Grpc[IO, Metadata]] =
    for {
      _ <- Proxy.resource[IO](Proxy.Config("127.0.0.1", 4444), 8888)
      _ <- Server.resource[IO](Server.Config(4444))
      c <- Client.resource[IO]("127.0.0.1", 8888)
    } yield c

  test("everything works") {
    val s =
      for {
        c <- Stream.resource(client)
        primes <- Stream.eval(c.primes(PrimeRequest.of(10), new Metadata).map(_.next).compile.toList)
      } yield assertEquals(primes, List(2, 3, 5, 7))

    s.compile.drain
  }

  test("invalid values return exceptions") {
    client.use { c =>
      c.primes(PrimeRequest.of(-1), new Metadata).compile.drain.attempt.flatMap {
        case Left(e: StatusRuntimeException) =>
          // normally asserting an error message is overfitting unless the error message is a specific part of the API
          // contract, but in this case the types of errors we can throw is pretty limited and it's important to be sure
          // that the error that the client gets is related to the specific error case we created; the exception that's
          // caught here isn't related to the type that the server threw, unfortunately, so we can't do much more than
          // inspect the message
          IO(assert(e.getMessage.contains("fewer than 0 primes")))
        case Left(_) =>  IO(fail("Unexpected exception"))
        case Right(_) => IO(fail("No exception"))
      }
    }
  }

}
