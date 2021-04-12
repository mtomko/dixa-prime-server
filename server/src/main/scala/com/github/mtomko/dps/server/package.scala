package com.github.mtomko.dps

import cats.syntax.all._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import fs2.{Pull, Pure, Stream}

package object server {

  // simple implementation of the Sieve of Eratosthenes for generating prime numbers; this models the sieve as an
  // infinite stream of `Int`, using a pull which recursively augments the filter applied to subsequent values
  val sieveOfEratosthenes: Stream[Pure, Int] = {
    def sieve(s: Stream[Pure, Int]): Pull[Pure, Int, Unit] =
      // the next element (`hd`) is prime because we've already eliminated all composites below it;
      // emit it then recursively filter the tail for any composites of `hd`
      s.pull.uncons1.flatMap {
        case Some((hd, tl)) => Pull.output1(hd) >> sieve(tl.filter(_ % hd =!= 0))
        case None           => Pull.done // this will never happen but that's okay
      }

    // as a _very_ minor optimization, emit 2 and then continue by generating only odd numbers
    sieve(Stream.emit(2) ++ Stream.iterate(3)(_ + 2)).stream
  }

  def primesUpTo(max: Int Refined Positive): Stream[Pure, Int] = sieveOfEratosthenes.takeWhile(_ <= max)

}
