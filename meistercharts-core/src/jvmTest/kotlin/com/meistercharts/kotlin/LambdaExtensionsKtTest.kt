package com.cedarsoft.kotlin

import assertk.*
import assertk.assertions.*
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import org.junit.jupiter.api.Test

class LambdaExtensionsKtTest {
  var p0: () -> String = { "myString" }
  var p1: (Double) -> String = { "myString" }
  var p2: (Double, Int) -> String = { _, _ -> "myString" }

  @Test
  fun testIt() {
    p0 = "asdf".asProvider()
    p1 = "asdf".asProvider1()

    assertThat(p0()).isEqualTo("asdf")
    assertThat(p1(4.0)).isEqualTo("asdf")
  }

  @Test
  fun testAsLambda() {
    p0 = "asdf".asProvider()
    p1 = "asdf".asProvider1()

    assertThat(p0()).isEqualTo("asdf")
    assertThat(p1(4.0)).isEqualTo("asdf")
  }
}
