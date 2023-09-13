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
import com.meistercharts.time.TimeRange
import com.meistercharts.history.impl.historyChunk
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import it.neckar.open.serialization.roundTrip
import it.neckar.open.test.utils.VirtualTime
import it.neckar.datetime.minimal.TimeConstants
import it.neckar.open.time.millis2Instant
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 */
class HistoryBucketDescriptorTest {
  @Disabled
  @Test
  fun testPringValues() {
    println(TimeConstants.referenceTimestamp.formatUtc())

    SamplingPeriod.entries.forEach { samplingPeriod ->
      println("--------------- $samplingPeriod --> ${samplingPeriod.toHistoryBucketRange()}")

      val descriptor = HistoryBucketDescriptor.forTimestamp(TimeConstants.referenceTimestamp, samplingPeriod)

      println("start: ${descriptor.start.formatUtc()}")
      println("end: ${descriptor.end.formatUtc()}")
      println()
    }

    println("--------")
    println("--------")
    println("--------")
    println()

    SamplingPeriod.entries.forEach { samplingPeriod ->
      println("--------------- $samplingPeriod --> ${samplingPeriod.toHistoryBucketRange()}")

      val descriptor = HistoryBucketDescriptor.forTimestamp(0.0, samplingPeriod)

      println("start: ${descriptor.start.formatUtc()}")
      println("end: ${descriptor.end.formatUtc()}")
      println()
    }
  }

  @Test
  fun name() {
    roundTrip(HistoryBucketDescriptor(44.0, HistoryBucketRange.OneMinute)){
      """
        {
          "index" : 44.0,
          "bucketRange" : "OneMinute",
          "start" : 2640000.0,
          "end" : 2700000.0
        }
      """.trimIndent()
    }
  }

  @Test
  fun testDescriptorsFromChunk() {
    val historyChunk = historyChunk(historyConfiguration {
      decimalDataSeries(DataSeriesId(99), TextKey.simple("asdf"))
    }) {
      addDecimalValues(10.0, 1.0)
      addDecimalValues(100.0, 1.0)
      addDecimalValues(1_000.0, 1.0)
      addDecimalValues(10_000.0, 1.0)
      addDecimalValues(100_000.0, 1.0)
      addDecimalValues(1_000_000.0, 1.0)
      addDecimalValues(10_000_000.0, 1.0)
      addDecimalValues(100_000_000.0, 1.0)
      addDecimalValues(1_000_000_000.0, 1.0)
    }

    historyChunk.calculateAllDescriptorsFor(SamplingPeriod.EveryHundredMillis).let {
      assertThat(it).hasSize(6)

      assertThat(it[0].start).isEqualTo(0.0)
      assertThat(it[1].start).isEqualTo(60_000.0)
      assertThat(it[2].start).isEqualTo(960_000.0)
      assertThat(it[3].start).isEqualTo(9_960_000.0)
      assertThat(it[4].start).isEqualTo(9.996E7)
      assertThat(it[5].start).isEqualTo(9.9996E8)
    }
  }

  @Test
  fun testDistance() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(100000.0, SamplingPeriod.EveryTenMillis)
    //Same
    assertThat(descriptor.distanceTo(descriptor)).isEqualTo(0.0)
    assertThat(descriptor.distanceTo(descriptor.next())).isEqualTo(1.0)
    assertThat(descriptor.distanceTo(descriptor.next(99))).isEqualTo(99.0)

    //other direction
    assertThat(descriptor.next().distanceTo(descriptor)).isEqualTo(-1.0)
  }

  @Test
  fun testForRange() {
    @ms val duration = 1000.0 * 60 * 10
    val timeRange = TimeRange.fromStartAndDuration(VirtualTime.defaultNow, duration)

    HistoryBucketDescriptor.forRange(timeRange.start, timeRange.end, HistoryBucketRange.TenMinutes).let {
      assertThat(timeRange.start.formatUtc()).isEqualTo("2021-03-27T21:45:23.002Z")
      assertThat(timeRange.end.formatUtc()).isEqualTo("2021-03-27T21:55:23.002Z")

      assertThat(it.first().start.formatUtc()).isEqualTo("2021-03-27T21:40:00.000Z")
      assertThat(it.last().end.formatUtc()).isEqualTo("2021-03-27T22:00:00.000Z")

      assertThat(it).hasSize(2)
    }
  }

  @Test
  fun testIncludeExclude() {
    val millis = 1312003123123.1234864
    assertThat(millis.formatUtc()).isEqualTo("2011-07-30T05:18:43.123Z")

    val descriptors = HistoryBucketDescriptor.forRange(millis, millis + 100 * 1000.0, SamplingPeriod.EveryTenMillis.toHistoryBucketRange())
    assertThat(descriptors).hasSize(21)

    assertThat(descriptors.first().start).isEqualToTimeStamp("2011-07-30T05:18:40.000Z")
    assertThat(descriptors.first().end).isEqualToTimeStamp("2011-07-30T05:18:45.000Z")

    assertThat(descriptors[1].start).isEqualToTimeStamp("2011-07-30T05:18:45.000Z")
    assertThat(descriptors[1].end).isEqualToTimeStamp("2011-07-30T05:18:50.000Z")

    assertThat(descriptors.last().start).isEqualToTimeStamp("2011-07-30T05:20:20.000Z")
    assertThat(descriptors.last().end).isEqualToTimeStamp("2011-07-30T05:20:25.000Z")
  }

  @Test
  internal fun testIt() {
    try {
      HistoryBucketDescriptor.forRange(0.0, Double.MAX_VALUE, SamplingPeriod.EveryTenMillis.toHistoryBucketRange())
      fail("Where is the exception?")
    } catch (e: Exception) {
    }
  }

  @Test
  internal fun testMillisToInstant() {
    val millis = 1312003123123.1234864
    assertThat(millis.formatUtc()).isEqualTo("2011-07-30T05:18:43.123Z")

    val instant = millis2Instant(millis)
    assertThat(instant.toDoubleMillis()).isEqualTo(millis)
  }

  @Test
  fun testContains() {
    val millis = 1312003123123.1234864
    assertThat(millis.formatUtc()).isEqualTo("2011-07-30T05:18:43.123Z")

    HistoryBucketRange.entries.forEach {
      val descriptor = HistoryBucketDescriptor.forTimestamp(millis, it)

      assertThat(descriptor.start).isEqualTo(descriptor.index * it.duration)

      assertThat(descriptor.start).isLessThanOrEqualTo(millis)
      assertThat(descriptor.end).isGreaterThan(millis)
      assertThat(descriptor.contains(millis)).isTrue()
    }
  }

  @Test
  internal fun testNext() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(10000007.0, HistoryBucketRange.FiveSeconds)

    assertThat(descriptor.start).isEqualTo(10000000.0)
    assertThat(descriptor.end).isEqualTo(10000000.0 + HistoryBucketRange.FiveSeconds.duration)

    assertThat(descriptor.next().start).isEqualTo(descriptor.end)
    assertThat(descriptor.next().end).isEqualTo(10000000.0 + HistoryBucketRange.FiveSeconds.duration * 2)

    assertThat(descriptor.next().start).isEqualTo(HistoryBucketRange.FiveSeconds.calculateStart(10000000.0 + HistoryBucketRange.FiveSeconds.duration))
  }

  @Test
  internal fun testPrevious() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(10000007.0, HistoryBucketRange.FiveSeconds)

    assertThat(descriptor.start).isEqualTo(10000000.0)
    assertThat(descriptor.end).isEqualTo(10000000.0 + HistoryBucketRange.FiveSeconds.duration)

    assertThat(descriptor.previous().end).isEqualTo(descriptor.start)
    assertThat(descriptor.previous().start).isEqualTo(10000000.0 - HistoryBucketRange.FiveSeconds.duration)

    assertThat(descriptor.previous().start).isEqualTo(HistoryBucketRange.FiveSeconds.calculateStart(10000000.0 - HistoryBucketRange.FiveSeconds.duration))
  }

  @Test
  fun testPreviousMultiple() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(10000007.0, HistoryBucketRange.FiveSeconds)

    assertThat(descriptor.start).isEqualTo(10000000.0)
    assertThat(descriptor.end).isEqualTo(10000000.0 + HistoryBucketRange.FiveSeconds.duration)

    assertThat(descriptor.previous(1).end).isEqualTo(descriptor.start)
    assertThat(descriptor.previous(1).start).isEqualTo(10000000.0 - HistoryBucketRange.FiveSeconds.duration)

    assertThat(descriptor.previous(10).start).isEqualTo(10000000.0 - HistoryBucketRange.FiveSeconds.duration * 10)
    assertThat(descriptor.previous(100).start).isEqualTo(10000000.0 - HistoryBucketRange.FiveSeconds.duration * 100)
  }

  @Test
  internal fun testNextNow() {
    val now = 1.578047448_083E12

    val descriptor = HistoryBucketDescriptor.forTimestamp(now, HistoryBucketRange.FiveSeconds)
    assertThat(descriptor.start).isEqualTo(1.578047445E12)
    assertThat(descriptor.end).isEqualTo(1.578047445E12 + HistoryBucketRange.FiveSeconds.duration)

    assertThat(descriptor.next().start).isEqualTo(descriptor.end)
    assertThat(descriptor.next().end).isEqualTo(1.578047445E12 + HistoryBucketRange.FiveSeconds.duration * 2)

    assertThat(descriptor.next().start).isEqualTo(HistoryBucketRange.FiveSeconds.calculateStart(1.578047448_000E12 + HistoryBucketRange.FiveSeconds.duration))

    assertThat(descriptor.next(7).start).isEqualTo(HistoryBucketRange.FiveSeconds.calculateStart(1.578047448_000E12 + HistoryBucketRange.FiveSeconds.duration * 7))
  }

  @Test
  fun testChildren() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(16_665_007.4, HistoryBucketRange.FiveSeconds)

    assertThat(descriptor.duration).isEqualTo(5_000.0)

    assertThat(descriptor.start).isEqualTo(16_665_000.0)
    assertThat(descriptor.end).isEqualTo(16_670_000.0)
    assertThat(descriptor.center).isEqualTo(16_667_500.0)

    val children = descriptor.children()
    assertThat(children.size).isEqualTo(50)

    assertThat(children[0].start).isEqualTo(descriptor.start)
    assertThat(children[49].end).isEqualTo(descriptor.end)

    children.forEachIndexed { index, it ->
      assertThat(it.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
      assertThat(it.duration).isEqualTo(100.0)

      assertThat(it.start).isEqualTo(descriptor.start + index * 100.0)
      assertThat(it.end).isEqualTo(descriptor.start + (index + 1) * 100.0)
    }
  }

  @Test
  fun testParent() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(16_665_007.4, HistoryBucketRange.FiveSeconds)

    val parent = descriptor.parent()
    require(parent != null)

    assertThat(parent.contains(descriptor.start)).isTrue()
    assertThat(parent.contains(descriptor.end)).isTrue()
    assertThat(parent.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)
  }

  @Test
  fun testChildrenStartEnd() {
    val now = 1.5900732415E12

    val descriptor = HistoryBucketDescriptor.forTimestamp(now, HistoryBucketRange.ThirtyDays)

    assertThat(now.formatUtc()).isEqualTo("2020-05-21T15:00:41.500Z")
    assertThat(descriptor.start.formatUtc()).isEqualTo("2020-05-08T00:00:00.000Z")
    assertThat(descriptor.end.formatUtc()).isEqualTo("2020-06-07T00:00:00.000Z")


    var currentDescriptor = descriptor


    //Check all levels
    while (currentDescriptor.bucketRange != HistoryBucketRange.smallestRange) {
      currentDescriptor.children().let { children ->
        //Check the number of children
        assertThat(children.isEmpty()).isFalse()

        val downSamplingFactor = currentDescriptor.bucketRange.downSamplingFactor()
        val firstChild = children.first()
        val lastChild = children.last()


        //All children together have as many entries as we times the down sampling factor
        val expectedNumberOfChildren = currentDescriptor.bucketRange.entriesCount * downSamplingFactor / firstChild.bucketRange.entriesCount

        assertThat(children.size, "Children for ${currentDescriptor.bucketRange}")
          .isEqualTo(expectedNumberOfChildren)


        //check the time ranges
        assertThat(firstChild.start).isEqualTo(currentDescriptor.start)
        assertThat(lastChild.end).isEqualTo(currentDescriptor.end)

        currentDescriptor = firstChild
      }
    }
  }

  @Test
  fun testChildrenBug() {
    val now = 1.5900732415E12
    val parentDescriptor = HistoryBucketDescriptor.forTimestamp(now, HistoryBucketRange.OneQuarter)

    parentDescriptor.children().let {
      assertThat(it, "parent: $parentDescriptor").isNotEmpty()
      assertThat(it.size).isGreaterThanOrEqualTo(2)

      assertThat(it.first().start).isEqualToTimeStamp(parentDescriptor.start)
      assertThat(it.last().end).isEqualToTimeStamp(parentDescriptor.end)
    }
  }

  @Test
  fun testChildrenAllLevels() {
    val now = 1.5900732415E12

    HistoryBucketRange.entries.forEach { bucketRange ->
      if (bucketRange == HistoryBucketRange.smallestRange) {
        return@forEach
      }

      val parentDescriptor = HistoryBucketDescriptor.forTimestamp(now, bucketRange)
      assertThat(parentDescriptor.bucketRange).isEqualTo(bucketRange)

      parentDescriptor.children().let {
        assertThat(it, "parent: $parentDescriptor").isNotEmpty()
        assertThat(it.size).isGreaterThanOrEqualTo(2)

        assertThat(it.first().start).isEqualToTimeStamp(parentDescriptor.start)
        assertThat(it.last().end).isEqualToTimeStamp(parentDescriptor.end)
      }
    }
  }
}
