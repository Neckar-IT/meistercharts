/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.toMillis
import it.neckar.open.time.millis2Instant
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

class HistoryBucketRangeTest {
  @Test
  fun testFind() {
    SamplingPeriod.entries.forEach {
      val found = HistoryBucketRange.find(it)
      assertThat(found).isNotNull()
      assertThat(found.samplingPeriod).isEqualTo(it)
    }
  }

  @Test
  fun testConstants() {
    assertThat(HistoryBucketRange.smallestRange).isSameAs(HistoryBucketRange.HundredMillis)
    assertThat(HistoryBucketRange.greatestRange).isSameAs(HistoryBucketRange.SevenHundredTwentyYears)
  }

  @Test
  internal fun testCalcDownSampling() {
    assertThat(HistoryBucketRange.FiveSeconds.downSamplingFactor()).isEqualTo(10)
    assertThat(HistoryBucketRange.OneMinute.downSamplingFactor()).isEqualTo(10)
    assertThat(HistoryBucketRange.TenMinutes.downSamplingFactor()).isEqualTo(10)
    assertThat(HistoryBucketRange.OneHour.downSamplingFactor()).isEqualTo(10)
    assertThat(HistoryBucketRange.SixHours.downSamplingFactor()).isEqualTo(6)
    assertThat(HistoryBucketRange.OneDay.downSamplingFactor()).isEqualTo(10)
    assertThat(HistoryBucketRange.ThirtyDays.downSamplingFactor()).isEqualTo(6)
  }

  @Test
  fun testDownSamplingSizes() {
    HistoryBucketRange.entries
      .drop(1)
      .forEach {
        val lower = it.lower()!!
        val packagesCount = lower.entriesCount.toDouble() / it.downSamplingFactor()

        val name = "Comparing <$it> with its lower <${it.lower()}>. Lower entries: ${lower.entriesCount}, down sampling factor: ${it.downSamplingFactor()} --> packages count: $packagesCount"
        assertThat(packagesCount % 1, name).isEqualTo(0.0)
      }
  }

  @Test
  internal fun testDurations() {
    verifyDuration(HistoryBucketRange.HundredMillis, 100.0)
    verifyDuration(HistoryBucketRange.FiveSeconds, 5_000.0)
    verifyDuration(HistoryBucketRange.OneMinute, 60 * 1_000.0)
    verifyDuration(HistoryBucketRange.TenMinutes, 10 * 60 * 1_000.0)
    verifyDuration(HistoryBucketRange.OneHour, 60 * 60 * 1_000.0)
    verifyDuration(HistoryBucketRange.SixHours, 6 * 60 * 60 * 1_000.0)
    verifyDuration(HistoryBucketRange.OneDay, 24 * 60 * 60 * 1_000.0)
    verifyDuration(HistoryBucketRange.ThirtyDays, 30 * 24 * 60 * 60 * 1_000.0)
  }

  private fun verifyDuration(range: HistoryBucketRange, expectedDuration: @ms Double) {
    assertThat(range.duration).isCloseTo(expectedDuration, 0.000001)
  }

  @Test
  internal fun testRoundStart() {
    val exactStart = HistoryBucketRange.OneMinute.calculateStart(1000000000.0)

    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart + 1)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart - 1)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart - 0.001)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart + 0.001)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart - Double.MIN_VALUE)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart + Double.MIN_VALUE)).isEqualTo(exactStart)

    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart - 1_000)).isEqualTo(exactStart)
    assertThat(HistoryBucketRange.OneMinute.roundStart(exactStart - 2_000)).isEqualTo(exactStart)
  }

  @Disabled
  @Test
  fun printDurations() {
    HistoryBucketRange.entries
      .forEachIndexed { index, level ->
        val duration = level.samplingPeriod.distance * level.entriesCount
        println("Level: $level -- $duration ms")
      }
  }

  @Test
  internal fun testPlausibility() {
    HistoryBucketRange.entries
      .forEachIndexed { index, level ->
        val duration = level.samplingPeriod.distance * level.entriesCount
        assertThat(level.duration).isEqualTo(duration)
      }
  }

  @Test
  internal fun testIt() {
    SamplingPeriod.entries.forEach {
      HistoryBucketRange.find(it)
    }
  }

  @Test
  internal fun testStarts() {
    val millis = 1312003123123.1234864
    val instant = millis2Instant(millis)
    assertThat(instant.toDoubleMillis()).isEqualTo(millis)

    assertThat(millis).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 18, 43, 123_123535, ZoneOffset.UTC).toMillis())

    assertThat(HistoryBucketRange.HundredMillis.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 18, 43, TimeUnit.MILLISECONDS.toNanos(100).toInt(), ZoneOffset.UTC).toMillis())

    assertThat(HistoryBucketRange.FiveSeconds.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 18, 40, 0, ZoneOffset.UTC).toMillis())
    assertThat(HistoryBucketRange.OneMinute.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 18, 0, 0, ZoneOffset.UTC).toMillis())
    assertThat(HistoryBucketRange.TenMinutes.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 10, 0, 0, ZoneOffset.UTC).toMillis())
    assertThat(HistoryBucketRange.OneHour.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 0, 0, 0, ZoneOffset.UTC).toMillis())
    assertThat(HistoryBucketRange.SixHours.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC).toMillis())
    assertThat(HistoryBucketRange.OneDay.calculateStart(millis)).isEqualTo(OffsetDateTime.of(2011, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC).toMillis())

    HistoryBucketRange.ThirtyDays.calculateStart(millis).let {
      assertThat(it, it.formatUtc()).isEqualTo(OffsetDateTime.of(2011, 7, 25, 0, 0, 0, 0, ZoneOffset.UTC).toMillis())
    }

  }

  @Test
  fun testGreatest() {
    HistoryBucketRange.entries.fastForEach {
      assertThat(HistoryBucketRange.greatestRange.duration).isGreaterThanOrEqualTo(it.duration)
    }
  }

  @Test
  fun testSmallest() {
    HistoryBucketRange.entries.fastForEach {
      assertThat(HistoryBucketRange.smallestRange.duration).isLessThanOrEqualTo(it.duration)
    }
  }

  @Test
  fun testUpper() {
    assertThat(HistoryBucketRange.greatestRange.upper()).isNull()
    HistoryBucketRange.entries.fastForEach {
      if (it != HistoryBucketRange.greatestRange) {
        val upper = it.upper()
        assertThat(upper).isNotNull()
        assertThat(upper!!.distance).isGreaterThan(it.distance)
      }
    }
  }

  @Test
  fun testLower() {
    assertThat(HistoryBucketRange.smallestRange.lower()).isNull()
    HistoryBucketRange.entries.fastForEach {
      if (it != HistoryBucketRange.smallestRange) {
        val lower = it.lower()
        assertThat(lower).isNotNull()
        assertThat(lower!!.distance).isLessThan(it.distance)
      }
    }
  }

  @Test
  fun testContains() {
    HistoryBucketRange.entries.fastForEach {
      if (it == HistoryBucketRange.smallestRange) {
        return@fastForEach
      }

      val lowerDuration = it.lower()!!.duration
      val duration = it.duration

      assertThat((duration / lowerDuration) % 1.0, "$it --> ${it.lower()}").isEqualTo(0.0)
    }
  }

  @Test
  fun testBug() {
    assertThat(HistoryBucketRange.ThirtyDays.entriesCount).isEqualTo(720)
    assertThat(HistoryBucketRange.ThirtyDays.samplingPeriod).isEqualTo(SamplingPeriod.EveryHour)

    assertThat(HistoryBucketRange.OneQuarter.entriesCount).isEqualTo(360)
    assertThat(HistoryBucketRange.OneQuarter.samplingPeriod).isEqualTo(SamplingPeriod.Every6Hours)
  }
}
