package com.meistercharts.algorithms.axis

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test

class TimeTickDistanceTest {
  @Test
  fun testMillis() {
    assertThat(DistanceMonths(1).dateTimeSpan.months).isEqualTo(1)
    assertThat(DistanceMonths(1).dateTimeSpan.days).isEqualTo(0)
  }

  @Test
  fun testOffset2ticks() {
    DistanceYears(1).smallestPossibleTickDistance().let {
      assertThat(it).isInstanceOf(DistanceDays::class)
    }
    DistanceMonths(1).smallestPossibleTickDistance().let {
      assertThat(it).isInstanceOf(DistanceDays::class)
    }
  }

  @Test
  fun testCoerceAtLeast() {
    DistanceMonths(1).coerceAtLeast(DistanceMillis(123.0)).let {
      assertThat(it).isInstanceOf(DistanceMonths::class)
    }
  }
}
