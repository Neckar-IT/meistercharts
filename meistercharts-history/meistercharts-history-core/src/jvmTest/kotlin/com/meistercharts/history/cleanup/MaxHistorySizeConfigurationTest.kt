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
package com.meistercharts.history.cleanup

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.SamplingPeriod
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

/**
 *
 */
class MaxHistorySizeConfigurationTest {
  @Test
  fun testBugReport() {
    assertThat(TimeUnit.SECONDS.toHours(86_400)).isEqualTo(24)
    val duration = 86_400 * 1000.0

    assertThat(SamplingPeriod.EveryMinute.toHistoryBucketRange()).isEqualTo(HistoryBucketRange.SixHours)
    assertThat(MaxHistorySizeConfiguration.forDuration(duration, SamplingPeriod.EveryMinute.toHistoryBucketRange()).keptBucketsCount).isEqualTo(5) //one more
  }

  @Test
  fun testDurationVerySmall() {
    assertThat(MaxHistorySizeConfiguration.forDuration(86_400.0, HistoryBucketRange.OneMinute).keptBucketsCount).isEqualTo(3)
  }

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
      assertThat(it.keptBucketsCount).isEqualTo(61)
      assertThat(it.getGuaranteedDuration(HistoryBucketRange.OneMinute)).isEqualTo(60 * 61 * 1000.0)
      assertThat(it.getGuaranteedTimeStampsCount(HistoryBucketRange.OneMinute)).isEqualTo(36_600)
    }
  }
}
