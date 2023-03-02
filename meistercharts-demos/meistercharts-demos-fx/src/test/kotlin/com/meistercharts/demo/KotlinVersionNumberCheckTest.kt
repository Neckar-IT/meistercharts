package com.meistercharts.demo

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 *
 */
class KotlinVersionNumberCheckTest {
  @Test
  fun testKotlinVersion() {
    assertThat(KotlinVersion.CURRENT).all {
      isLessThan(KotlinVersion(1, 9))
      isGreaterThan(KotlinVersion(1, 7))
    }

  }

  @Test
  fun testMeasureNotNotFoundException() {
    val measureTime = measureTime {
      Thread.sleep(2)
    }

    assertThat(measureTime.toDouble(DurationUnit.MILLISECONDS)).isGreaterThan(1.0)
  }
}
