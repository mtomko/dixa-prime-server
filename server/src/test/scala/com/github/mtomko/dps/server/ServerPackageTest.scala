package com.github.mtomko.dps.server

import cats.syntax.all._
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import eu.timepit.refined.scalacheck.numeric._
import munit.{CatsEffectSuite, ScalaCheckSuite}
import org.scalacheck.Prop._

class ServerPackageTest extends CatsEffectSuite with ScalaCheckSuite {

  test("sieveOfEratosthenes example") {
    assertEquals(sieveOfEratosthenes.takeWhile(_ < 20).toList, List(2, 3, 5, 7, 11, 13, 17, 19))
  }

  property("values in primesUpTo are not factors of one another") {
    val min: Int Refined Positive = 2
    val max: Int Refined Positive = 100
    forAll(chooseRefinedNum(min, max)) { (max: Int Refined Positive) =>
      // the generator guarantees `primes` is non-empty, but scalacheck shrinking sometimes causes the generators to
      // disregard their boundaries
      val primes = primesUpTo(max).compile.toVector
      primes.headOption.foreach { _ =>
        val last = primes.last
        val rest = primes.dropRight(1)
        assert(rest.forall(last % _ =!= 0))
      }
    }
  }

  test("primesUpTo does not contain composites of other values") {
    val min: Int Refined Positive = 2
    val max: Int Refined Positive = 10
    forAll(chooseRefinedNum(min, max), chooseRefinedNum(min, max)) {
      (x: Int Refined Positive, y: Int Refined Positive) =>
        // x or y may be prime, but x * y is composite
        refineV[Positive](x * y) match {
          case Left(_) => fail("this cannot happen")
          case Right(composite) =>
            val primes = primesUpTo(composite)

            // primes up to `composite` cannot contain a known composite number
            primes.compile.last.forall(_ =!= composite)
        }
    }
  }

}
