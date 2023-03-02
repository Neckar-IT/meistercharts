package com.meistercharts.algorithms.layers

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThanOrEqualTo
import com.meistercharts.algorithms.axis.TimeAxisTickCalculator
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test

/**
 *
 */
class TimeAxisLayerTest {
  private val nowForTests: @ms Double = 1593510070000.0.also {
    assertThat(it.formatUtc()).isEqualTo("2020-06-30T09:41:10.000")
  }

  @Test
  fun ensureStability() {
    val layer = TimeAxisLayer()

    TimeAxisTickCalculator.calculateTickValues(
      nowForTests,
      nowForTests + 1000.0,
      240.0,
      TimeZone.UTC
    ).let { result ->
      assertThat(result).hasSize(5)
      assertThat(result[0].formatUtc()).isEqualTo("2020-06-30T09:41:10.000")
      assertThat(result[1].formatUtc()).isEqualTo("2020-06-30T09:41:10.250")
      assertThat(result[2].formatUtc()).isEqualTo("2020-06-30T09:41:10.500")
      assertThat(result[3].formatUtc()).isEqualTo("2020-06-30T09:41:10.750")
      assertThat(result[4].formatUtc()).isEqualTo("2020-06-30T09:41:11.000")
    }


    for (minTickDistance in 140..260 step 20) {
      var first = 0.0
      var last = 0.0

      for (timeOffset in 0..1000 step 50) {
        TimeAxisTickCalculator.calculateTickValues(
          nowForTests + timeOffset,
          nowForTests + 1000.0 + timeOffset,
          minTickDistance.toDouble(),
          TimeZone.UTC
        ).let { result ->
          assertThat(first).isLessThanOrEqualTo(result.first())
          assertThat(last).isLessThanOrEqualTo(result.last())

          first = result.first()
          last = result.last()
        }
      }
    }

  }
}
