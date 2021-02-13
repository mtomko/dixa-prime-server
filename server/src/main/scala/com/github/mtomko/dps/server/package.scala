package com.github.mtomko.dps

import cats.syntax.all._
import fs2.{Pull, Pure, Stream}

package object server {

  val sieveOfEratosthenes: Stream[Pure, Int] = {
    def sieve(s: Stream[Pure, Int]): Pull[Pure, Int, Unit] =
      s.pull.uncons1.flatMap {
        case Some((hd, tl)) => Pull.output1(hd) >> sieve(tl.filter(_ % hd =!= 0))
        case None           => Pull.done // this will never happen but that's okay
      }

    sieve(Stream.iterate(2)(_ + 1)).stream
  }

}
