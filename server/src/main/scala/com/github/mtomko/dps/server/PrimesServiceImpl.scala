package com.github.mtomko.dps.server

import cats.MonadThrow
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import fs2.Stream
import io.grpc.Metadata
import prime.{PrimeRequest, PrimeResponse, PrimesServiceFs2Grpc}

class PrimesServiceImpl[F[_]: MonadThrow] extends PrimesServiceFs2Grpc[F, Metadata] {
  override def primes(request: PrimeRequest, ctx: Metadata): Stream[F, PrimeResponse] =
    refineV[Positive](request.upTo) match {
      case Left(_)     => Stream.raiseError(PrimeException("Cannot request primes less than 0"))
      case Right(upTo) => primesUpTo(upTo).map(PrimeResponse.of)
    }
}
