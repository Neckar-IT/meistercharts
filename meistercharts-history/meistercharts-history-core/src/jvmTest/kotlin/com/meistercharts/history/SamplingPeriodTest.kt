package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class SamplingPeriodTest {
  @Test
  fun testWithMaxDistance() {
    assertThat(SamplingPeriod.withMaxDistance(100.0)).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(SamplingPeriod.withMaxDistance(99.0)).isEqualTo(SamplingPeriod.EveryTenMillis)
    assertThat(SamplingPeriod.withMaxDistance(1.0)).isEqualTo(SamplingPeriod.EveryMillisecond)
    assertThat(SamplingPeriod.withMaxDistance(2.0)).isEqualTo(SamplingPeriod.EveryMillisecond)
  }

  @Test
  fun testGetForDistance() {
    assertThat {
      SamplingPeriod.getForDistance(2.0)
    }.isFailure()

    assertThat(SamplingPeriod.getForDistance(10.0)).isEqualTo(SamplingPeriod.EveryTenMillis)
    assertThat(SamplingPeriod.getForDistance(100.0)).isEqualTo(SamplingPeriod.EveryHundredMillis)
    assertThat(SamplingPeriod.getForDistance(60 * 1000.0)).isEqualTo(SamplingPeriod.EveryMinute)
  }

  @Test
  fun testAbove() {
    assertThat(SamplingPeriod.EverySecond.above()).isSameAs(SamplingPeriod.EveryTenSeconds)

    SamplingPeriod.values().forEach {
      if (it.ordinal == SamplingPeriod.values().size - 1) {
        return@forEach
      }

      assertThat(it.above()).isSameAs(SamplingPeriod.values()[it.ordinal + 1])
    }
  }

  @Test
  internal fun testMaxFactor() {
    SamplingPeriod.values().forEachIndexed { index, samplingPeriod ->
      if (index == 0) {
        return@forEachIndexed
      }
      val previous = SamplingPeriod.values()[index - 1]
      val factor = samplingPeriod.distance / previous.distance

      assertThat(factor).all("Factor: $factor -- from $previous to $samplingPeriod") {
        isBetween(2.0, 10.0)
      }
    }
  }

  @Test
  internal fun testGetByDistance() {
    SamplingPeriod.values().forEach {
      assertThat(SamplingPeriod.getForDistance(it.distance)).isSameAs(it)
    }
  }

  @Test
  fun testFindNiceBordersMilliSeconds() {
    assertThat(SamplingPeriod.EveryMillisecond.sameOrGreater(1.0)).isEqualTo(1.0)
    assertThat(SamplingPeriod.EveryMillisecond.sameOrGreater(1.4)).isEqualTo(2.0)
    assertThat(SamplingPeriod.EveryMillisecond.sameOrGreater(1.9)).isEqualTo(2.0)
    assertThat(SamplingPeriod.EveryMillisecond.sameOrGreater(2.0)).isEqualTo(2.0)

    assertThat(SamplingPeriod.EveryMillisecond.sameOrSmaller(1.0)).isEqualTo(1.0)
    assertThat(SamplingPeriod.EveryMillisecond.sameOrSmaller(1.4)).isEqualTo(1.0)
    assertThat(SamplingPeriod.EveryMillisecond.sameOrSmaller(1.9)).isEqualTo(1.0)
    assertThat(SamplingPeriod.EveryMillisecond.sameOrSmaller(2.0)).isEqualTo(2.0)
  }

  @Test
  fun testFindNiceBordersTenMilliSeconds() {
    assertThat(SamplingPeriod.EveryTenMillis.sameOrGreater(1.0)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrGreater(1.4)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrGreater(1.9)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrGreater(2.0)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrGreater(19.0)).isEqualTo(20.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrGreater(10.0)).isEqualTo(10.0)

    assertThat(SamplingPeriod.EveryTenMillis.sameOrSmaller(10.0)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrSmaller(14.0)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrSmaller(19.0)).isEqualTo(10.0)
    assertThat(SamplingPeriod.EveryTenMillis.sameOrSmaller(20.0)).isEqualTo(20.0)
  }

  @Test
  fun testFindNiceBordersHundredMilliseconds() {
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(1.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(1.4)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(1.9)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(2.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(19.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(10.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(39.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(40.0)).isEqualTo(100.0)

    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(99.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(100.0)).isEqualTo(100.0)
    assertThat(SamplingPeriod.EveryHundredMillis.sameOrGreater(101.0)).isEqualTo(200.0)
  }

  @Test
  fun testFindNiceBordersTenMinutesSeconds() {
    val tenMinutes = 1000 * 10 * 60.0

    assertThat(SamplingPeriod.EveryTenMinutes.sameOrGreater(tenMinutes)).isEqualTo(tenMinutes)
    assertThat(SamplingPeriod.EveryTenMinutes.sameOrGreater(tenMinutes + 1)).isEqualTo(tenMinutes * 2)
    assertThat(SamplingPeriod.EveryTenMinutes.sameOrGreater(tenMinutes * 2)).isEqualTo(tenMinutes * 2)

    assertThat(SamplingPeriod.EveryTenMinutes.sameOrSmaller(tenMinutes * 2)).isEqualTo(tenMinutes * 2)
    assertThat(SamplingPeriod.EveryTenMinutes.sameOrSmaller(tenMinutes * 2 - 1)).isEqualTo(tenMinutes)
  }
}
