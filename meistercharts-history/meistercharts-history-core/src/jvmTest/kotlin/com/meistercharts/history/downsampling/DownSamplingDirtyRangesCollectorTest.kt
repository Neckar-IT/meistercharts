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
import com.meistercharts.time.TimeRange
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.*
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DownSamplingDirtyRangesCollectorTest {
  val now: @ms Double = 1.5900732415E12

  @BeforeEach
  fun setUp() {
    assertThat(now.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
  }

  @Test
  fun testMerge() {
    val dirtyRangesCollector = DownSamplingDirtyRangesCollector()

    val descriptor = HistoryBucketDescriptor.forTimestamp(now, SamplingPeriod.EveryHundredMillis)

    val dirtySamplingPeriod = descriptor.bucketRange.samplingPeriod.above()!!
    assertThat(dirtySamplingPeriod).isEqualTo(SamplingPeriod.EverySecond)


    dirtyRangesCollector.markAsDirty(dirtySamplingPeriod, TimeRange(now, now))
    assertThat(dirtyRangesCollector[dirtySamplingPeriod]!!).all {
      hasSize(1)

      first {
        given {
          assertThat(it.firstStart).isEqualTo(now)
          assertThat(it.lastEnd).isEqualTo(now)
        }
      }
    }

    dirtyRangesCollector.markAsDirty(dirtySamplingPeriod, TimeRange(now + 50.0, now + 50.0))
    assertThat(dirtyRangesCollector[dirtySamplingPeriod]!!).all {
      hasSize(1)

      first {
        given {
          assertThat(it.firstStart).isEqualTo(now)
          assertThat(it.lastEnd).isEqualTo(now + 50.0)
        }
      }
    }
  }

  @Test
  internal fun testBasics() {
    val downSamplingService = DownSamplingDirtyRangesCollector()

    assertThat(downSamplingService[SamplingPeriod.EveryHundredMillis]).isNull()
    assertThat(downSamplingService[SamplingPeriod.EverySecond]).isNull()

    downSamplingService.markAsDirty(SamplingPeriod.EverySecond, TimeRange(10_000.0, 20_000.0))

    assertThat(downSamplingService[SamplingPeriod.EveryHundredMillis]).isNull()
    downSamplingService[SamplingPeriod.EverySecond]!!.let {
      assertThat(it.isEmpty()).isFalse()
      assertThat(it[0].start).isEqualTo(10_000.0)
      assertThat(it[0].end).isEqualTo(20_000.0)
    }
  }

  @Test
  fun testApiTest() {
    val historyStorage = InMemoryHistoryStorage()

    val descriptor = HistoryBucketDescriptor.forTimestamp(now, SamplingPeriod.EveryHundredMillis)

    val chunk = createDemoChunkOnlyDecimals(descriptor) { dsIndex, timestampIndex ->
      (dsIndex.value * timestampIndex.value).toDouble()
    }

    val downSamplingService = DownSamplingDirtyRangesCollector()

    assertThat(downSamplingService[SamplingPeriod.EveryHundredMillis]).isNull()
    assertThat(downSamplingService[SamplingPeriod.EverySecond]).isNull()

    //mark as dirty
    downSamplingService.observe(historyStorage)

    historyStorage.storeWithoutCache(chunk, SamplingPeriod.EveryHundredMillis)
    assertThat(downSamplingService[SamplingPeriod.EveryHundredMillis]).isNull()

    downSamplingService[SamplingPeriod.EverySecond]!!.let {
      assertThat(it.isEmpty()).isFalse()
      assertThat(it[0].start.formatUtc()).isEqualTo(chunk.firstTimestamp.formatUtc())
      assertThat(it[0].end.formatUtc()).isEqualTo(chunk.lastTimestamp.formatUtc())
    }
  }
}
