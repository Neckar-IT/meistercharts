package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.nowMillis
import org.junit.jupiter.api.Test

/**
 */
internal class CurrentTimeMillisJvmKtTest {
  @Test
  internal fun testGetMillis() {
    val result = nowMillis()
    assertThat(result).isGreaterThan(1554327478872.0)
  }
}

