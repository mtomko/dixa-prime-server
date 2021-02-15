package com.github.mtomko.dps.test

import cats.effect.{IO, Resource}
import com.github.mtomko.dps.proxy.Proxy
import com.github.mtomko.dps.server.Server
import munit.CatsEffectSuite
import org.http4s.Status
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

class IntegrationTest extends CatsEffectSuite {

  val client: Resource[IO, ProxyClient[IO]] =
    for {
      _ <- Proxy.resource[IO](Proxy.Config("127.0.0.1", 4444), 8888)
      _ <- Server.resource[IO](Server.Config(4444))
      blazeClient <- BlazeClientBuilder[IO](global).resource
    } yield new ProxyClient[IO](ProxyClient.Config("127.0.0.1", 8888), blazeClient)

  test("everything works") {
    client.use { c =>
      assertIO(c.primes(20.toString).compile.toList, List(2, 3, 5, 7, 11, 13, 17, 19))
    }
  }

  test("invalid integers yield 400") {
    client.use { c =>
      c.primes(-1.toString).compile.drain.attempt.flatMap {
        case Left(ProxyClient.ProxyError(status, _)) => IO(assertEquals(status, Status.BadRequest))
        case Left(e) =>  IO(fail(s"Unexpected exception: $e"))
        case Right(_) => IO(fail("No exception"))
      }
    }
  }

  test("non integers yield 404") {
    client.use { c =>
      c.primes("this is not a number").compile.drain.attempt.flatMap {
        case Left(ProxyClient.ProxyError(status, _)) => IO(assertEquals(status, Status.NotFound))
        case Left(e) =>  IO(fail(s"Unexpected exception: $e"))
        case Right(_) => IO(fail("No exception"))
      }
    }
  }

}
