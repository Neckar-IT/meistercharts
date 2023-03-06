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
package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test


internal class TimeRangeTest {
  val nowForTests: Double = 1.5900732415E12.also {
    assertThat(it.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
  }

  @Test
  internal fun testExtend() {
    val timeRange = TimeRange(10.0, 20.0)

    timeRange.extend(5.0).let {
      assertThat(it.start).isEqualTo(5.0)
      assertThat(it.end).isEqualTo(25.0)
    }
  }

  @Test
  fun testContains2() {
    TimeRange(10.0, 20.0).let {
      assertThat(it.contains(10.0)).isTrue()
      assertThat(it.contains(20.0)).isTrue()

      assertThat(it.contains(9.9)).isFalse()
      assertThat(it.contains(20.1)).isFalse()

      assertThat(it.contains(10.0, 20.0)).isTrue()
      assertThat(it.contains(10.0, 20.1)).isFalse()
      assertThat(it.contains(9.9, 20.1)).isFalse()
      assertThat(it.contains(9.9, 20.0)).isFalse()
    }
  }

  @Test
  fun testFit() {
    TimeRange(1.0, 5.0).fitWithin(1.0, 5.0).let {
      assertThat(it.start).isEqualTo(1.0)
      assertThat(it.end).isEqualTo(5.0)
    }

    TimeRange(1.0, 5.0).fitWithin(2.0, 7.0).let {
      assertThat(it.start).isEqualTo(2.0)
      assertThat(it.end).isEqualTo(5.0)
    }

    TimeRange(1.0, 5.0).fitWithin(0.5, 7.0).let {
      assertThat(it.start).isEqualTo(1.0)
      assertThat(it.end).isEqualTo(5.0)
    }

    TimeRange(1.0, 5.0).fitWithin(3.5, 4.0).let {
      assertThat(it.start).isEqualTo(3.5)
      assertThat(it.end).isEqualTo(4.0)
    }
  }

  @Test
  fun testCompress() {
    val start0 = nowForTests
    val end0 = nowForTests + 1000
    val timeRange0 = TimeRange(start0, end0)

    val start1 = nowForTests
    val end1 = nowForTests + 2000
    val timeRange1 = TimeRange(start1, end1)

    TimeRange.compress(listOf(timeRange0, timeRange1)).let { compressed ->
      assertThat(compressed).hasSize(1)
      assertThat(compressed[0].start.formatUtc()).isEqualTo(start0.formatUtc())
      assertThat(compressed[0].end.formatUtc()).isEqualTo(end1.formatUtc())
    }
  }

  @Test
  fun testCompressWithGap() {
    val start0 = nowForTests
    val end0 = nowForTests + 1000
    val timeRange0 = TimeRange(start0, end0)

    val start1 = end0 + 10
    val end1 = start1 + 100
    val timeRange1 = TimeRange(start1, end1)

    TimeRange.compress(listOf(timeRange0, timeRange1), 10.0).let { compressed ->
      assertThat(compressed).hasSize(1)
      assertThat(compressed[0].start.formatUtc()).isEqualTo(start0.formatUtc())
      assertThat(compressed[0].end.formatUtc()).isEqualTo(end1.formatUtc())
    }
  }

  @Test
  fun testMerge3() {
    val start0 = nowForTests
    val end0 = nowForTests + 1000
    val update0 = TimeRange(start0, end0)

    val start1 = nowForTests
    val end1 = nowForTests + 2000
    val update1 = TimeRange(start1, end1)

    TimeRange.compress(listOf(update0, update1)).let {
      assertThat(it).hasSize(1)
      assertThat(it[0].start.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
      assertThat(it[0].end.formatUtc()).isEqualTo("2020-05-21T15:00:43.500")
    }
  }

  @Test
  fun testMerge2() {
    val updateInfo1 = TimeRange(nowForTests, nowForTests + 1002).also {
      assertThat(it.start.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
      assertThat(it.end.formatUtc()).isEqualTo("2020-05-21T15:00:42.502")
    }

    val updateInfo2 = TimeRange(nowForTests + 500, nowForTests + 1702).also {
      assertThat(it.start.formatUtc()).isEqualTo("2020-05-21T15:00:42.000")
      assertThat(it.end.formatUtc()).isEqualTo("2020-05-21T15:00:43.202")
    }

    updateInfo1.merge(updateInfo2).also {
      assertThat(it.start.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
      assertThat(it.end.formatUtc()).isEqualTo("2020-05-21T15:00:43.202")
    }
  }

  @Test
  fun testMerge() {
    val start0 = 1.5900732715E12
    val start0Formatted = "2020-05-21T15:01:11.500"
    assertThat(start0.formatUtc()).isEqualTo(start0Formatted)
    val end0 = start0 + 1000
    val end0Formatted = "2020-05-21T15:01:12.500"
    assertThat(end0.formatUtc()).isEqualTo(end0Formatted)

    val timeRange = TimeRange(start0, end0)

    timeRange.merge(TimeRange(start0 + 100, end0)).let {
      assertThat(it.start.formatUtc()).isEqualTo(start0Formatted)
      assertThat(it.end.formatUtc()).isEqualTo(end0Formatted)
    }

    timeRange.merge(TimeRange(start0 + 100, end0 - 100)).let {
      assertThat(it.start.formatUtc()).isEqualTo(start0Formatted)
      assertThat(it.end.formatUtc()).isEqualTo(end0Formatted)
    }

    timeRange.merge(TimeRange(start0, end0)).let {
      assertThat(it.start.formatUtc()).isEqualTo(start0Formatted)
      assertThat(it.end.formatUtc()).isEqualTo(end0Formatted)
    }

    TimeRange(start0 + 100, end0 - 100).merge(timeRange).let {
      assertThat(it.start.formatUtc()).isEqualTo(start0Formatted)
      assertThat(it.end.formatUtc()).isEqualTo(end0Formatted)
    }

    TimeRange(start0, end0 - 100).merge(timeRange).let {
      assertThat(it.start.formatUtc()).isEqualTo(start0Formatted)
      assertThat(it.end.formatUtc()).isEqualTo(end0Formatted)
    }

    TimeRange(start0 + 100, end0).merge(timeRange).let {
      assertThat(it.start.formatUtc()).isEqualTo(start0Formatted)
      assertThat(it.end.formatUtc()).isEqualTo(end0Formatted)
    }
  }

  /**
   * Sat Jan 01 0000 00:53:28 GMT+0053 (Central European Standard Time)
   */
  @ms
  val millisSmall: Double = -62167219200000.0

  @Test
  internal fun testCompareTo() {
    val offset = millisSmall + 1_000.0
    assertThat(TimeRange(offset + 1, offset + 2).compareTo(TimeRange(offset + 1, offset + 2))).isEqualTo(0)
    assertThat(TimeRange(offset + 0, offset + 0).compareTo(TimeRange(offset + 0, offset + 0))).isEqualTo(0)

    assertThat(TimeRange(offset + 0, offset + 1).compareTo(TimeRange(offset + 2, offset + 3))).isLessThan(0)
    assertThat(TimeRange(offset + 0, offset + 1).compareTo(TimeRange(offset + 1, offset + 2))).isLessThan(0)
    assertThat(TimeRange(offset + 0, offset + 1).compareTo(TimeRange(offset + 0, offset + 2))).isLessThan(0)
    assertThat(TimeRange(offset + 0, offset + 1).compareTo(TimeRange(offset - 1, offset + 2))).isLessThan(0)

    assertThat(TimeRange(offset + 2, offset + 3).compareTo(TimeRange(offset + 0, offset + 1))).isGreaterThan(0)
    assertThat(TimeRange(offset + 1, offset + 2).compareTo(TimeRange(offset + 0, offset + 1))).isGreaterThan(0)
    assertThat(TimeRange(offset + 0, offset + 2).compareTo(TimeRange(offset + 0, offset + 1))).isGreaterThan(0)
    assertThat(TimeRange(offset - 1, offset + 2).compareTo(TimeRange(offset + 0, offset + 1))).isGreaterThan(0)
  }

  @Test
  fun testOverlap() {
    val timestamp = millisSmall + 1_000.0
    assertThat(TimeRange(timestamp, timestamp).isOverlapping(TimeRange(timestamp, timestamp))).isTrue()

    assertThat(TimeRange(timestamp, timestamp).isOverlapping(TimeRange(timestamp - 1, timestamp - 1))).isFalse()
    assertThat(TimeRange(timestamp - 1, timestamp - 1).isOverlapping(TimeRange(timestamp, timestamp))).isFalse()

    assertThat(TimeRange(timestamp, timestamp).isOverlapping(TimeRange(timestamp, timestamp + 1))).isTrue()
    assertThat(TimeRange(timestamp, timestamp + 1).isOverlapping(TimeRange(timestamp, timestamp))).isTrue()

    assertThat(TimeRange(timestamp, timestamp).isOverlapping(TimeRange(timestamp - 1, timestamp + 1))).isTrue()
    assertThat(TimeRange(timestamp - 1, timestamp + 1).isOverlapping(TimeRange(timestamp, timestamp))).isTrue()

    assertThat(TimeRange(timestamp - 1, timestamp + 1).isOverlapping(TimeRange(timestamp - 10, timestamp + 10))).isTrue()
    assertThat(TimeRange(timestamp - 10, timestamp + 10).isOverlapping(TimeRange(timestamp - 1, timestamp + 1))).isTrue()
  }

  @Test
  fun testAdjacent() {
    val timestamp = 10000000000000.0

    // this equals other
    assertThat(TimeRange(timestamp, timestamp).isAdjacent(TimeRange(timestamp, timestamp))).isTrue()
    // this before other
    assertThat(TimeRange(timestamp - 100.0, timestamp - 50.0).isAdjacent(TimeRange(timestamp - 50.0, timestamp))).isTrue()
    // this after other
    assertThat(TimeRange(timestamp - 50.0, timestamp).isAdjacent(TimeRange(timestamp - 100.0, timestamp - 50.0))).isTrue()
    // this before other and not adjacent
    assertThat(TimeRange(timestamp - 100.0, timestamp - 60.0).isAdjacent(TimeRange(timestamp - 50.0, timestamp))).isFalse()
    // this after other and not adjacent
    assertThat(TimeRange(timestamp - 50.0, timestamp).isAdjacent(TimeRange(timestamp - 100.0, timestamp - 60.0))).isFalse()
  }

  @Test
  fun testAdjacentWithEpsilon() {
    val timestamp = 10000000000000.0

    // this equals other
    assertThat(TimeRange(timestamp, timestamp).isAdjacent(TimeRange(timestamp, timestamp), 0.00001)).isTrue()
    // this before other
    assertThat(TimeRange(timestamp - 100.0, timestamp - 50.0).isAdjacent(TimeRange(timestamp - 50.0, timestamp), 0.00001)).isTrue()
    // this after other
    assertThat(TimeRange(timestamp - 50.0, timestamp).isAdjacent(TimeRange(timestamp - 100.0, timestamp - 50.0), 0.00001)).isTrue()
    // this before other and not adjacent and epsilon too small
    assertThat(TimeRange(timestamp - 100.0, timestamp - 60.0).isAdjacent(TimeRange(timestamp - 50.0, timestamp), 0.00001)).isFalse()
    // this after other and not adjacent and epsilon too small
    assertThat(TimeRange(timestamp - 50.0, timestamp).isAdjacent(TimeRange(timestamp - 100.0, timestamp - 60.0), 0.00001)).isFalse()
    // this before other and not adjacent and epsilon large enough
    assertThat(TimeRange(timestamp - 100.0, timestamp - 60.0).isAdjacent(TimeRange(timestamp - 50.0, timestamp), 10.0)).isTrue()
    // this after other and not adjacent and epsilon large enough
    assertThat(TimeRange(timestamp - 50.0, timestamp).isAdjacent(TimeRange(timestamp - 100.0, timestamp - 60.0), 10.0)).isTrue()
  }

  @Test
  fun testAlign() {
    val timestamp = 10000000000000.0

    assertThat(TimeRange(timestamp, timestamp).align(1.0)).isEqualTo(TimeRange(timestamp, timestamp))
    assertThat(TimeRange(timestamp, timestamp).align(100.0)).isEqualTo(TimeRange(timestamp, timestamp + 99.0))

    assertThat(TimeRange(timestamp - 17.0, timestamp + 377.0).align(100.0)).isEqualTo(TimeRange(timestamp - 100.0, timestamp + 399.0))

    assertThat(TimeRange(timestamp - 17.0, timestamp + 17.0).align(100.0)).isEqualTo(TimeRange(timestamp - 100.0, timestamp + 99.0))

    assertThat(TimeRange(timestamp - 17.0, timestamp + 511.0).align(500.0)).isEqualTo(TimeRange(timestamp - 500.0, timestamp + 999.0))

    assertThat(TimeRange(timestamp - 517.0, timestamp + 377.0).align(500.0)).isEqualTo(TimeRange(timestamp - 1000.0, timestamp + 499.0))

    assertThat(TimeRange(timestamp - 0.1, timestamp + 0.1).align(100.0)).isEqualTo(TimeRange(timestamp - 100.0, timestamp + 99.0))

    assertThat(TimeRange(timestamp + 99.0, timestamp + 99.1).align(100.0)).isEqualTo(TimeRange(timestamp, timestamp + 199.0))
  }

}
