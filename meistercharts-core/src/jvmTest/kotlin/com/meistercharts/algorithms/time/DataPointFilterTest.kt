package com.meistercharts.algorithms.time

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.time.DataPointFilter.DomainValueExtractor
import org.junit.jupiter.api.Test

/**
 */
internal class DataPointFilterTest {
  @Test
  internal fun testBasics() {
    val dataPoint = DataPoint(123.0, doubleArrayOf(1.0, 2.0))
    val dataPoint1 = DataPoint(123.0, doubleArrayOf(4.0, 5.0))

    val filter = DataPointFilter(0.0, 1000.0, 0.0, 100.0, DomainValueExtractor<DoubleArray> { it.value[0] })

    assertThat(filter.findRelevantForPaint(listOf(dataPoint))).hasSize(1)
    assertThat(filter.findRelevantForPaint(listOf(dataPoint, dataPoint1))).hasSize(2)
  }
}
