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
package com.meistercharts.history.generator

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.TimeRange
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.RandomWithSeed
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.RecordingType
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

/**
 *
 */
class HistoryChunkGeneratorTest {

  private val now = 1.5900732415E12

  @Test
  fun testNow() {
    assertThat(now.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
  }

  @Test
  fun testBasics() {
    val historyStorage = InMemoryHistoryStorage()

    val historyChunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = SamplingPeriod.EveryHundredMillis,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = List(3) {
        EnumValueGenerator { _, _ -> HistoryEnumSet.first }
      },
      referenceEntryGenerators = emptyList(),
    )

    assertThat(historyChunkGenerator.totalDataSeriesCount).isEqualTo(3)

    val historyConfiguration = historyChunkGenerator.historyConfiguration

    assertThat(historyConfiguration.decimalDataSeriesCount).isEqualTo(0)
    assertThat(historyConfiguration.enumDataSeriesCount).isEqualTo(3)
    assertThat(historyConfiguration.enumConfiguration.getEnum(EnumDataSeriesIndex.one)).isEqualTo(HistoryEnum.Boolean)


    historyChunkGenerator.forTimeRange(TimeRange.fromEndAndDuration(now, 2.seconds)).let { chunk ->
      assertNotNull(chunk)
      assertThat(chunk.recordingType).isEqualTo(RecordingType.Measured)
      assertThat(chunk.timeStampsCount).isEqualTo(20)
      assertThat(chunk.start.formatUtc()).isEqualTo("2020-05-21T15:00:39.500")
      assertThat(chunk.end.formatUtc()).isEqualTo("2020-05-21T15:00:41.400")

      assertThat(chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(HistoryEnumSet.first)

      if (false) {
        println(chunk.dump())
      }
    }
  }

  @Test
  fun testRefEntriesAlwaysNoValue() {
    val historyStorage = InMemoryHistoryStorage()

    val generator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = SamplingPeriod.EveryHundredMillis,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = List(3) {
        ReferenceEntryGenerator.always(ReferenceEntryId.NoValue)
      },
    )

    val chunk = generator.forTimeRange(TimeRange.oneHourUntilNow())
    requireNotNull(chunk)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex.one)).isEqualTo(ReferenceEntryId.NoValue)
  }

  @RandomWithSeed(123)
  @Test
  fun testRefEntries() {
    val historyStorage = InMemoryHistoryStorage()

    val generator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = SamplingPeriod.EveryHundredMillis,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = List(3) {
        ReferenceEntryGenerator.random()
      },
    )

    val chunk = generator.forTimeRange(TimeRange.oneHourUntilNow())
    requireNotNull(chunk)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex.one)).isEqualTo(ReferenceEntryId(79304))
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(123))).isEqualTo(ReferenceEntryId(62225))
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(124))).isEqualTo(ReferenceEntryId(27945))
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(124))).isEqualTo(ReferenceEntryId(69906))

    assertThat(chunk.getReferenceEntriesDataMap(ReferenceEntryDataSeriesIndex.zero)).isInstanceOf(ReferenceEntriesDataMap.Generated::class)
  }
}
