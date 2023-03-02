package com.meistercharts.history.cleanup

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketRange
import org.junit.jupiter.api.Test

/**
 *
 */
class MaxHistorySizeConfigurationTest {
  @Test
  fun testIt() {
    MaxHistorySizeConfiguration(17).let {
      assertThat(it.keptBucketsCount).isEqualTo(17)
      assertThat(it.getGuaranteedDuration(HistoryBucketRange.OneMinute)).isEqualTo(1_020_000.0)
      assertThat(it.getGuaranteedTimeStampsCount(HistoryBucketRange.OneMinute)).isEqualTo(10_200)
    }

    MaxHistorySizeConfiguration.maxEntries(10_000, HistoryBucketRange.OneMinute).let {
      assertThat(it.keptBucketsCount).isEqualTo(17)
      assertThat(it.getGuaranteedDuration(HistoryBucketRange.OneMinute)).isEqualTo(1_020_000.0)
      assertThat(it.getGuaranteedTimeStampsCount(HistoryBucketRange.OneMinute)).isEqualTo(10_200)
    }

    MaxHistorySizeConfiguration.forDuration(60 * 60 * 1000.0 - 7.0, HistoryBucketRange.OneMinute).let {
      assertThat(it.keptBucketsCount).isEqualTo(60)
      assertThat(it.getGuaranteedDuration(HistoryBucketRange.OneMinute)).isEqualTo(60 * 60 * 1000.0)
      assertThat(it.getGuaranteedTimeStampsCount(HistoryBucketRange.OneMinute)).isEqualTo(36_000)
    }
  }
}
