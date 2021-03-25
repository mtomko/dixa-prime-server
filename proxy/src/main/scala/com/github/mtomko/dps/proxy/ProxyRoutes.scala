package com.github.mtomko.dps.proxy

import cats.{Applicative, Defer}
import io.grpc.Metadata
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import prime.{PrimeRequest, PrimesServiceFs2Grpc}

object ProxyRoutes {

  def primeRoutes[F[_]: Applicative: Defer](primes: PrimesServiceFs2Grpc[F, Metadata]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      // `IntVar` means that calls to, eg, GET /prime/notanumber will return 404 (Not Found)
      // this behavior is debatable, depending on what kind of information you want to send back to your clients;
      // if you want to obscure your routes, this is a good choice; if you want to return a detailed message to your
      // clients explaining the API, you would not want to use `IntVar`, then validate `number` here and return that
      // description
      case GET -> Root / "prime" / IntVar(number) =>
        if (number < 0) BadRequest(s"Cannot request primes less than 0")
        else Ok(primes.primes(PrimeRequest.of(number.toInt), new Metadata).map(_.next.toString))
    }
  }

}
