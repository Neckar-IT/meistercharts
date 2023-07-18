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
import com.meistercharts.history.DefaultReferenceEntriesDataMap
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.isEqualToHistoryEnumSet
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
    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.zero)).isEqualToHistoryEnumSet(0b1100011)
  }

  @RandomWithSeed(seed = 351)
  @Test
  fun testDownSampling() {
    val historyStorage = InMemoryHistoryStorage()

    val samplingPeriod = SamplingPeriod.EveryHundredMillis

    val historyChunkGenerator = HistoryChunkGenerator(historyStorage = historyStorage, samplingPeriod = samplingPeriod,

      decimalValueGenerators = emptyList(), enumValueGenerators = emptyList(), referenceEntryGenerators = listOf(
        ReferenceEntryGenerator.random()
      ), referenceEntryStatusProvider = { referenceEntryId: ReferenceEntryId, millis: Double ->
        if ((millis / samplingPeriod.distance) % 2 >= 1.0) {
          HistoryEnumSet.first
        } else {
          HistoryEnumSet.second
        }
      })

    assertThat(historyChunkGenerator.historyConfiguration.decimalDataSeriesCount).isEqualTo(0)
    assertThat(historyChunkGenerator.historyConfiguration.enumDataSeriesCount).isEqualTo(0)
    assertThat(historyChunkGenerator.historyConfiguration.referenceEntryDataSeriesCount).isEqualTo(1)


    val recordedChunkLarge = historyChunkGenerator.forTimeRange(TimeRange.oneMinuteSinceReference)
    requireNotNull(recordedChunkLarge)
    assertThat(recordedChunkLarge).isNotNull()

    (recordedChunkLarge.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).let {
      assertThat(it.entries).hasSize(572)
      assertThat(recordedChunkLarge.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToHistoryEnumSet(0b10)
      assertThat(recordedChunkLarge.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1))).isEqualToHistoryEnumSet(0b01)
      assertThat(recordedChunkLarge.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(77))).isEqualToHistoryEnumSet(0b01)
    }

    val recordedChunkDescriptors = HistoryBucketDescriptor.fromChunk(recordedChunkLarge, samplingPeriod)
    assertThat(recordedChunkDescriptors).hasSize(1)

    val recordedBuckets = recordedChunkDescriptors.mapNotNull {
      val historyChunk = recordedChunkLarge.range(it.start, it.end)
      if (historyChunk != null) {
        HistoryBucket(it, historyChunk)
      } else {
        null
      }
    }

    assertThat(recordedBuckets).hasSize(1)

    recordedBuckets[0].let { bucket ->
      assertThat((bucket.chunk.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(572)

      val historyChunk = bucket.chunk

      println(historyChunk.dump())

      assertThat(bucket.start.formatUtc()).isEqualTo("2024-01-01T00:00:00.000")
      assertThat(bucket.end.formatUtc()).isEqualTo("2024-01-01T00:01:00.000")
      assertThat(historyChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToReferenceEntryId(25912)
      assertThat(historyChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1))).isEqualToReferenceEntryId(30701)


      requireNotNull(historyChunk.getReferenceEntryData(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryId(25912))).let { data ->
        assertThat(data.id).isEqualToReferenceEntryId(25912)
        assertThat(data.label.key).isEqualTo("Label 25912")
      }

      assertThat(historyChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToHistoryEnumSet(0b10)
      assertThat(historyChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1))).isEqualToHistoryEnumSet(0b01)
    }

    val descriptorForDownSampling = HistoryBucketDescriptor.forTimestamp(TimeConstants.referenceTimestamp, samplingPeriod.above()!!)
    val downSampled = descriptorForDownSampling.calculateDownSampled(recordedBuckets)

    assertThat(downSampled.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToReferenceEntryId(25912)
    assertThat(downSampled.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(400))).isEqualTo(ReferenceEntryId.Pending)

    downSampled.chunk.getReferenceEntryData(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryId(25912)).let {
      requireNotNull(it)
      assertThat(it.id).isEqualToReferenceEntryId(25912)
    }

    assertThat(downSampled.chunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToHistoryEnumSet(0b11)
    assertThat(downSampled.chunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(400))).isEqualTo(HistoryEnumSet.Pending)
    assertThat(downSampled.chunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(401))).isEqualTo(HistoryEnumSet.Pending)
    assertThat(downSampled.chunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(402))).isEqualTo(HistoryEnumSet.Pending)
  }
}
