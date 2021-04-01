package com.github.mtomko.dps.proxy

import cats.effect.Async
import com.github.mtomko.dps.client.Client
import fs2.Stream
import io.grpc.Metadata
import prime.{PrimeRequest, PrimeResponse, PrimesServiceFs2Grpc}

class PrimesServiceProxyImpl[F[_]: Async](config: Proxy.Config) extends PrimesServiceFs2Grpc[F, Metadata] {

  override def primes(request: PrimeRequest, ctx: Metadata): Stream[F, PrimeResponse] =
    Stream.resource(Client.resource[F](config.targetIp, config.targetPort)).flatMap { c =>
      c.primes(request, ctx)
    }

}
