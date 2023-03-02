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
