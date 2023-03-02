package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.TimestampIndex
import org.junit.jupiter.api.Test

class TimestampIndexTest {
  @Test
  fun testCompare() {
    assertThat(TimestampIndex(7)).isGreaterThan(TimestampIndex(6))
    assertThat(TimestampIndex(7)).isLessThan(TimestampIndex(8))
    assertThat(TimestampIndex(7)).isLessThanOrEqualTo(TimestampIndex(7))
    assertThat(TimestampIndex(7)).isGreaterThanOrEqualTo(TimestampIndex(7))
  }
}
