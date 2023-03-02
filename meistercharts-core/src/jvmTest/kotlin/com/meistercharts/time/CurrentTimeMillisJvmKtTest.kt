package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.nowMillis
import org.junit.jupiter.api.Test

/**
 */
internal class CurrentTimeMillisJvmKtTest {
  @Test
  internal fun testIt() {
    val time = nowMillis()
    assertThat(time).isGreaterThan(35153153.0)
  }
}
