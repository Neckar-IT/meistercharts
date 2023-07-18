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
package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import com.meistercharts.time.TimeRange
import com.meistercharts.time.TimeRanges
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.VirtualTime
import it.neckar.open.test.utils.first
import it.neckar.open.test.utils.last
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class DownSamplingServiceTest {
  lateinit var historyStorage: InMemoryHistoryStorage

  @BeforeEach
  internal fun setUp() {
    historyStorage = InMemoryHistoryStorage()
  }

  @Test
  fun testIt() {

    val dirtyRangesCollector = DownSamplingDirtyRangesCollector()

    val range = TimeRange.fromStartAndDuration(nowForTests, 1000.0)
    val samplingPeriod = SamplingPeriod.EveryMillisecond

    dirtyRangesCollector.markAsDirty(samplingPeriod, range)

    dirtyRangesCollector[samplingPeriod]!!.let {
      assertThat(it).isNotNull()
      assertThat(it.timeRanges).hasSize(1)
      assertThat(it.isEmpty()).isFalse()
    }

    dirtyRangesCollector.remove(samplingPeriod).let {
      assertThat(it).isNotNull().hasSize(1)
    }

    //Check the dirty range has been removed
    dirtyRangesCollector[samplingPeriod].let {
      assertThat(it).isNull()
    }
  }

  @Test
  fun testCreateJobs2() {
    val downSamplingService = DownSamplingService(historyStorage)

    downSamplingService.createJobs(TimeRanges.of(TimeRange(102.0, 480.0)), SamplingPeriod.EveryHundredMillis.toHistoryBucketRange()).let {
      (it.first().refreshRange as RefreshPartially).let {
        assertThat(it.timeRanges.firstStart).isEqualTo(102.0)
        assertThat(it.timeRanges.lastEnd).isEqualTo(480.0)
      }
    }

    @ms val duration = 1000.0 * 60 * 10
    downSamplingService.createJobs(TimeRanges.of(TimeRange.fromStartAndDuration(VirtualTime.defaultNow, duration)), HistoryBucketRange.TenMinutes).let {
      assertThat(it).hasSize(2)

      (it.first().refreshRange as RefreshPartially).let {
        assertThat(it.timeRanges.firstStart.formatUtc()).isEqualTo("2021-03-27T21:45:23.002")
        assertThat(it.timeRanges.lastEnd.formatUtc()).isEqualTo("2021-03-27T21:50:00.000")
      }

      (it.last().refreshRange as RefreshPartially).let {
        assertThat(it.timeRanges.firstStart.formatUtc()).isEqualTo("2021-03-27T21:50:00.000")
        assertThat(it.timeRanges.lastEnd.formatUtc()).isEqualTo("2021-03-27T21:55:23.002")
      }
    }
  }

  @Test
  fun testCreateJobs() {
    val downSamplingService = DownSamplingService(historyStorage)

    downSamplingService.createJobs(TimeRanges.empty, HistoryBucketRange.FiveSeconds).let {
      assertThat(it).isEmpty()
    }

    downSamplingService.createJobs(TimeRanges.of(TimeRange(102.0, 480.0)), HistoryBucketRange.HundredMillis).let {
      assertThat(it).all {
        isNotEmpty()
        hasSize(4)

        each {
          it.given { job ->
            assertThat(job.descriptor.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
          }
        }

        first {
          it.isRefreshPartially()
        }
        last {
          it.isRefreshPartially()
        }

        given { jobs ->
          //The first should be a partial
          jobs.first().let { firstJob ->
            assertThat(firstJob.descriptor.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)

            val refreshRange = firstJob.refreshRange
            assertThat(refreshRange).isInstanceOf(RefreshPartially::class)

            val refreshPartially = refreshRange as RefreshPartially
            assertThat(refreshPartially.timeRanges.firstStart).isEqualTo(102.0)
            assertThat(refreshPartially.timeRanges.lastEnd).isEqualTo(200.0)
          }

          //between should be complete
          assertThat(jobs[1].refreshRange).isEqualTo(RefreshCompletely)
          assertThat(jobs[2].refreshRange).isEqualTo(RefreshCompletely)

          //The last should be a partial
          jobs.last().let { firstJob ->
            assertThat(firstJob.descriptor.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)

            val refreshRange = firstJob.refreshRange
            assertThat(refreshRange).isInstanceOf(RefreshPartially::class)

            val refreshPartially = refreshRange as RefreshPartially
            assertThat(refreshPartially.timeRanges.firstStart).isEqualTo(400.0)
            assertThat(refreshPartially.timeRanges.lastEnd).isEqualTo(480.0)
          }
        }
      }
    }
  }
}

private fun Assert<DownSamplingJob>.isRefreshPartially() = given {
  if (it.refreshRange is RefreshPartially) return
  expected("Expected to be RefreshPartially but was <${it.refreshRange::class}>")
}

private fun Assert<DownSamplingJob>.isRefreshCompletely() = given {
  if (it.refreshRange is RefreshCompletely) return
  expected("Expected to be RefreshCompletely but was <${it.refreshRange::class}>")
}
