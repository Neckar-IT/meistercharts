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
package com.meistercharts.time

import assertk.*
import assertk.assertions.*
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

/**
 *
 */
class TimeRangesTest {
  @Test
  fun testSerialization() {
    roundTrip(TimeRanges(listOf(TimeRange(10001.0, 400000.0)) )) {
      """
  {
    "timeRanges" : [ {
      "start" : 10001.0,
      "end" : 400000.0
    } ]
  }
      """.trimIndent()
    }

  }

  @Test
  fun testSimple() {
    val timeRanges = TimeRanges(listOf(TimeRange(1.0, 2.0)))
    assertThat(timeRanges.timeRanges).hasSize(1)
  }

  @Test
  fun testMerged() {
    val timeRangesList = listOf(TimeRange(1.0, 2.0), TimeRange(2.0, 3.0))
    TimeRanges(timeRangesList).also {
      assertThat(it.timeRanges).hasSize(2)
    }

    TimeRanges.createMerged(timeRangesList).also {
      assertThat(it.timeRanges).hasSize(1)
      assertThat(it.timeRanges[0].start).isEqualTo(1.0)
      assertThat(it.timeRanges[0].end).isEqualTo(3.0)
    }
  }

  @Test
  fun testToString() {
    TimeRanges(listOf(TimeRange(1.0, 2.0), TimeRange(2.0, 3.0))).let {
      assertThat(it.firstStart).isEqualTo(1.0)
      assertThat(it.lastEnd).isEqualTo(3.0)
    }
  }

  @Test
  fun testMergeExtended() {
    val timeRangesList = listOf(TimeRange(1.0, 2.0), TimeRange(5.0, 10.0))
    TimeRanges(timeRangesList).also {
      assertThat(it.timeRanges).hasSize(2)
    }

    //is not merged!
    assertThat(TimeRanges.createMerged(timeRangesList)).hasSize(2)

    TimeRanges.createMerged(timeRangesList, 3.0).also {
      assertThat(it.timeRanges).hasSize(1)
      assertThat(it.timeRanges[0].start).isEqualTo(1.0)
      assertThat(it.timeRanges[0].end).isEqualTo(10.0)
    }
  }
}
