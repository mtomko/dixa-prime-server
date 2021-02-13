package com.github.mtomko.dps.server

import munit.{FunSuite, ScalaCheckSuite}

class ServerPackageTest extends FunSuite with ScalaCheckSuite {

  test("sieveOfEratosthenes example") {
    assertEquals(sieveOfEratosthenes.takeWhile(_ < 20).toList, List(2, 3, 5, 7, 11, 13, 17, 19))
  }

}