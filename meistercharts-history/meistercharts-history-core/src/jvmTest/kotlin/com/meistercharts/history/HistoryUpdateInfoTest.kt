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
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.TimeRanges
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.*

class HistoryUpdateInfoTest {

  @Test
  fun mergeOnEmpty() {
    val range1 = TimeRange(1000.0,1100.0)
    val range2 = TimeRange(1100.0,1200.0)
    val historyUpdateInfo = HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, TimeRanges.empty)

    val update = historyUpdateInfo.merge(range1).merge(range2)
    assertThat(update.updatedTimeRanges).isNotEmpty()
    assertThat(update.updatedTimeRanges.size).isEqualTo(1) // is 1 because Time Ranges were merged into 1
    assertThat(update.updatedTimeRanges[0].start).isEqualTo(1000.0)
    assertThat(update.updatedTimeRanges[0].end).isEqualTo(1200.0)
  }

  @Test
  fun merge() {
    val range1 = TimeRange(1000.0,1100.0)
    val range2 = TimeRange(1200.0,1300.0)
    val historyUpdateInfo = HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, range1)

    val update = historyUpdateInfo.merge(range2)
    assertThat(update.updatedTimeRanges.timeRanges.size).isEqualTo(2) // is 2 because Time Ranges could not be merged into 1
  }

  @Test
  fun mergeOverlap() {
    val range1 = TimeRange(1000.0,1100.0)
    val range2 = TimeRange(1050.0,1300.0)
    val historyUpdateInfo = HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, range1)

    val update = historyUpdateInfo.merge(range2)
    assertThat(update.updatedTimeRanges.timeRanges.size).isEqualTo(1) // is 1 because Time Ranges were merged into 1 (ranges overlap)
    assertThat(update.updatedTimeRanges[0].start).isEqualTo(1000.0)
    assertThat(update.updatedTimeRanges[0].end).isEqualTo(1300.0)
  }

}
