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
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.history.DefaultReferenceEntriesDataMap
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.isEqualToReferenceEntryId
import com.meistercharts.history.isEqualToReferenceEntryIdsCount
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.RandomWithSeed
import it.neckar.open.time.TimeConstants
import org.junit.jupiter.api.Test

class DownSamplingReferenceEntryCalculatorTest {
  @Test
  fun testIt() {
    val calculator = DownSamplingCalculator(0, 0, 1)

    calculator.addReferenceEntrySample(
      newReferenceEntries = intArrayOf(7),
      newDifferentIdsCount = intArrayOf(15),
      newStatuses = intArrayOf(99)
    )

    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.zero)).isEqualToReferenceEntryId(7)
    assertThat(calculator.referenceEntryDifferentIdsCount(ReferenceEntryDataSeriesIndex.zero)).isEqualToReferenceEntryIdsCount(15)
  }

  @RandomWithSeed(seed = 351)
  @Test
  fun testDownSampling() {
    val historyStorage = InMemoryHistoryStorage()

    val samplingPeriod = SamplingPeriod.EveryHundredMillis

    val historyChunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = samplingPeriod,

      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = listOf(
        ReferenceEntryGenerator.random()
      )
    )

    assertThat(historyChunkGenerator.historyConfiguration.decimalDataSeriesCount).isEqualTo(0)
    assertThat(historyChunkGenerator.historyConfiguration.enumDataSeriesCount).isEqualTo(0)
    assertThat(historyChunkGenerator.historyConfiguration.referenceEntryDataSeriesCount).isEqualTo(1)


    val chunk = historyChunkGenerator.forTimeRange(TimeRange.oneMinuteSinceReference)
    requireNotNull(chunk)
    assertThat(chunk).isNotNull()

    (chunk.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).let {
      assertThat(it.entries).hasSize(572)
    }

    val descriptors = HistoryBucketDescriptor.fromChunk(chunk, samplingPeriod)
    assertThat(descriptors).hasSize(2)

    val buckets = descriptors.mapNotNull {
      val historyChunk = chunk.range(it.start, it.end)
      if (historyChunk != null) {
        HistoryBucket(it, historyChunk)
      } else {
        null
      }
    }

    assertThat(buckets).hasSize(2)

    buckets[0].let { bucket ->
      assertThat((bucket.chunk.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(190)

      val historyChunk = bucket.chunk

      println(historyChunk.dump())

      assertThat(bucket.start.formatUtc()).isEqualTo("2001-09-09T01:46:00.000")
      assertThat(bucket.end.formatUtc()).isEqualTo("2001-09-09T01:47:00.000")
      assertThat(historyChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToReferenceEntryId(25912)
      assertThat(historyChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1))).isEqualToReferenceEntryId(30701)


      requireNotNull(historyChunk.getReferenceEntryData(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryId(25912))).let { data ->
        assertThat(data.id).isEqualToReferenceEntryId(25912)
        assertThat(data.label.key).isEqualTo("Label 25912")
      }
    }
    buckets[1].let { bucket ->
      assertThat((bucket.chunk.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(384)
      val historyChunk = bucket.chunk

      println(historyChunk.dump())
      assertThat(bucket.start.formatUtc()).isEqualTo("2001-09-09T01:47:00.000")
      assertThat(bucket.end.formatUtc()).isEqualTo("2001-09-09T01:48:00.000")

      assertThat(historyChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToReferenceEntryId(23372)
      assertThat(historyChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1))).isEqualToReferenceEntryId(88193)

      assertThat(historyChunk.getReferenceEntryData(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryId(23372))).isNotNull()
      assertThat(historyChunk.getReferenceEntryData(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryId(88193))).isNotNull()
    }

    val descriptorForDownSampling = HistoryBucketDescriptor.forTimestamp(TimeConstants.referenceTimestamp, samplingPeriod.above()!!)
    val downSampled = descriptorForDownSampling.calculateDownSampled(buckets)

    assertThat(downSampled.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(ReferenceEntryId.Pending)
    assertThat(downSampled.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(400))).isEqualToReferenceEntryId(25912)

    downSampled.chunk.getReferenceEntryData(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryId(25912)).let {
      requireNotNull(it)
      assertThat(it.id).isEqualToReferenceEntryId(25912)
    }
  }
}
