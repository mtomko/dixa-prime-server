package com.github.mtomko.dps.server

import cats.effect.{Sync, Timer}
import fs2.Stream
import io.grpc.Metadata
import prime.{PrimeRequest, PrimeResponse, PrimesServiceFs2Grpc}
//import scala.concurrent.duration._

class PrimesServiceImpl[F[_]: Sync: Timer] extends PrimesServiceFs2Grpc[F, Metadata] {
  override def primes(request: PrimeRequest, ctx: Metadata): Stream[F, PrimeResponse] =
    if (request.upTo < 0) Stream.raiseError(PrimeException(s"Cannot request fewer than 0 primes (${request.upTo})"))
    else {
      sieveOfEratosthenes
        .takeWhile(_ < request.upTo)
        .covary[F]
        //.metered(100.millis) // this is just for testing purposes
        .map(PrimeResponse.of)
    }
}
