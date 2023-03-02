package com.meistercharts.fx.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test

/**
 */
class CurrentTimeFxTest {
  @Test
  internal fun testMillis() {
    @ms val now = nowMillis()
    assertThat(now).isGreaterThan(351531.0)
  }
}
