package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import it.neckar.open.formatting.formatUtc
import org.junit.jupiter.api.Test

class DownSamplingSimpleTest {
  private val historyConfiguration = historyConfiguration {
    referenceEntryDataSeries(DataSeriesId(17), "Series A", HistoryEnum.Active)
  }

  @Test
  fun testDownSampling() {
    val samplingPeriodDownSampled = SamplingPeriod.EveryHundredMillis.above()
    assertThat(samplingPeriodDownSampled).isEqualTo(SamplingPeriod.EverySecond)

    val downSampledDescriptor = HistoryBucketDescriptor.forTimestamp(nowForTests, samplingPeriodDownSampled!!.toHistoryBucketRange())

    val childDescriptor0 = downSampledDescriptor.children()[0]
    val childDescriptor1 = downSampledDescriptor.children()[1]


    assertThat(childDescriptor0.start.formatUtc()).isEqualTo("2020-05-21T15:00:00.000")
    assertThat(childDescriptor0.end.formatUtc()).isEqualTo("2020-05-21T15:01:00.000")
    assertThat(childDescriptor1.start.formatUtc()).isEqualTo("2020-05-21T15:01:00.000")
    assertThat(childDescriptor1.end.formatUtc()).isEqualTo("2020-05-21T15:02:00.000")

    val bucket0 = historyConfiguration.chunk {
      addReferenceEntryValues(childDescriptor0.start, 7, referenceEntryStatuses = intArrayOf(0b01))
      addReferenceEntryValues(childDescriptor0.start + 101, 8, referenceEntryStatuses = intArrayOf(0b001))
    }.toBucket(childDescriptor0)

    val bucket1 = historyConfiguration.chunk {
      addReferenceEntryValues(childDescriptor1.start, 9, referenceEntryStatuses = intArrayOf(0b100))
      addReferenceEntryValues(childDescriptor1.start + 101, 10, referenceEntryStatuses = intArrayOf(0b1100))
    }.toBucket(childDescriptor1)


    val downSampled = downSampledDescriptor.calculateDownSampled(listOf(bucket0, bucket1))

    assertThat(downSampled.start.formatUtc()).isEqualTo("2020-05-21T15:00:00.000")
    assertThat(downSampled.end.formatUtc()).isEqualTo("2020-05-21T15:10:00.000")

    println(downSampled.chunk.dump())
  }
}
