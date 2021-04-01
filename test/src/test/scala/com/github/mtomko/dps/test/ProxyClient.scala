package com.github.mtomko.dps.test

import cats.effect.Sync
import cats.syntax.all._
import fs2.{text, Stream}
import org.http4s.Method._
import org.http4s.UriTemplate.PathElm
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{Response, Status, Uri, UriTemplate}

class ProxyClient[F[_]: Sync](config: ProxyClient.Config, client: Client[F]) {

  private val dsl = new Http4sClientDsl[F] {}
  import dsl._

  private def handleError(r: Response[F]): Stream[F, Response[F]] =
    if (r.status === Status.Ok) Stream.emit(r)
    else {
      val f: F[Response[F]] = r.body
        .adaptError(e => ProxyClient.ProxyError(r.status, e.getMessage))
        .through(text.utf8Decode)
        .compile
        .toList
        .map(_.mkString)
        .flatMap(text => Sync[F].raiseError(ProxyClient.ProxyError(r.status, text)))
      Stream.eval(f)
    }

  // one wouldn't normally allow this in a client, but we need to be able to get invalid inputs to the server
  // in our test
  def primes(max: String): Stream[F, Int] =
    Stream
      .emit(GET(config.uri.addSegment(max)))
      .flatMap(client.stream)
      .flatMap(handleError)
      .flatMap(_.body.through(text.utf8Decode).filter(_.nonEmpty))
      .map(_.toInt)

}

object ProxyClient {

  final case class ProxyError(status: Status, message: String) extends Exception(s"$status: $message")

  final case class Config(name: String, port: Int) {
    lazy val uri: Uri = UriTemplate(
      authority = Some(Uri.Authority(host = Uri.RegName(name), port = Some(port))),
      scheme = Some(Uri.Scheme.http),
      path = List(PathElm("prime"))
    ).toUriIfPossible.get

  }

}
