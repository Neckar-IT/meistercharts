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
package com.meistercharts.api.discrete

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test

class DiscreteCalculationsTest {
  @Test
  fun testEmpty() {
    assertThat(DiscreteTimelineChartData(emptyArray(), 10_000.0).toChunk(HistoryConfiguration.empty)).isNull()
  }

  @Test
  fun testCombine() {
    val dataForSeries0 = DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(1000.0, 2000.0, "the label", 1.0),
        DiscreteDataEntry(2000.0, 4000.0, "the label2", 2.0),
      )
    )
    val dataForSeries1 = DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(800.0, 1800.0, "the label", 1.0),
        DiscreteDataEntry(2200.0, 4500.0, "the label2", 2.0),
      )
    )

    @ms val timestamps = arrayOf(dataForSeries0, dataForSeries1).allTimestampsForChunk(800.0, 4600.0, SamplingPeriod.EveryHundredMillis).toList()

    assertThat(timestamps).containsExactly(
      800.0,
      900.0,
      1000.0,
      1100.0,
      1200.0,
      1300.0,
      1400.0,
      1500.0,
      1600.0,
      1700.0,
      1800.0,
      1900.0,
      2000.0,
      2100.0,
      2200.0,
      2300.0,
      2400.0,
      2500.0,
      2600.0,
      2700.0,
      2800.0,
      2900.0,
      3000.0,
      3100.0,
      3200.0,
      3300.0,
      3400.0,
      3500.0,
      3600.0,
      3700.0,
      3800.0,
      3900.0,
      4000.0,
      4100.0,
      4200.0,
      4300.0,
      4400.0,
      4500.0,
      4600.0
    )
  }

  @Test
  fun testTimestampsForSamplingPeriod() {
    timestampsForSamplingPeriod(1000.0, 2000.0, SamplingPeriod.EveryHundredMillis).let {
      assertThat(it.toList()).containsExactly(1000.0, 1100.0, 1200.0, 1300.0, 1400.0, 1500.0, 1600.0, 1700.0, 1800.0, 1900.0, 2000.0)
    }

    timestampsForSamplingPeriod(1000.0, 2001.0, SamplingPeriod.EveryHundredMillis).let {
      assertThat(it.toList()).containsExactly(1000.0, 1100.0, 1200.0, 1300.0, 1400.0, 1500.0, 1600.0, 1700.0, 1800.0, 1900.0, 2000.0, 2001.0)
    }
  }

  @Test
  fun testTimestampsFromEntryBounds() {
    val dataForSeries0 = DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(1000.0, 2000.0, "the label", 1.0),
        DiscreteDataEntry(2000.0, 4000.0, "the label2", 2.0),
      )
    )
    val dataForSeries1 = DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(500.0, 1800.0, "the label", 1.0),
        DiscreteDataEntry(2200.0, 4500.0, "the label2", 2.0),
      )
    )

    val timestamps = arrayOf(dataForSeries0, dataForSeries1).timestampsFromEntryBounds().toList()
    assertThat(timestamps).containsExactly(500.0, 1000.0, 1800.0, 2000.0, 2200.0, 4000.0, 4500.0)
  }

  @Test
  fun testFindTimestamp() {
    val data = DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(1000.0, 2000.0, "the label", 1.0),
        DiscreteDataEntry(2000.0, 4000.0, "the label2", 2.0),
      )
    )

    assertThat(data.findEntryIndexForTimestamp(2001.0).index).isEqualTo(1)

    assertThat(data.findEntryIndexForTimestamp(0.0)).isEqualTo(EntryIndexSearchResult.NotFound)
    assertThat(data.findEntryIndexForTimestamp(1000.0).index).isEqualTo(0)
    assertThat(data.findEntryIndexForTimestamp(1001.0).index).isEqualTo(0)
    assertThat(data.findEntryIndexForTimestamp(1999.0).index).isEqualTo(0)
    assertThat(data.findEntryIndexForTimestamp(2000.0).index).isEqualTo(1)
    assertThat(data.findEntryIndexForTimestamp(2001.0).index).isEqualTo(1)
    assertThat(data.findEntryIndexForTimestamp(3999.0).index).isEqualTo(1)
    assertThat(data.findEntryIndexForTimestamp(4000.0)).isEqualTo(EntryIndexSearchResult.NotFound) //exclusive!
  }

  @Test
  fun testFindTimestampGap() {
    val data = DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(1000.0, 1800.0, "the label", 1.0),
        //Gap!
        DiscreteDataEntry(2000.0, 4000.0, "the label2", 2.0),
      )
    )

    assertThat(data.findEntryIndexForTimestamp(0.0)).isEqualTo(EntryIndexSearchResult.NotFound)
    assertThat(data.findEntryIndexForTimestamp(1000.0).index).isEqualTo(0)
    assertThat(data.findEntryIndexForTimestamp(1001.0).index).isEqualTo(0)
    assertThat(data.findEntryIndexForTimestamp(1799.0).index).isEqualTo(0)

    assertThat(data.findEntryIndexForTimestamp(1800.0)).isEqualTo(EntryIndexSearchResult.NotFound) //Gap
    assertThat(data.findEntryIndexForTimestamp(1999.0)).isEqualTo(EntryIndexSearchResult.NotFound) //gap

    assertThat(data.findEntryIndexForTimestamp(2000.0).index).isEqualTo(1)
    assertThat(data.findEntryIndexForTimestamp(2001.0).index).isEqualTo(1)
    assertThat(data.findEntryIndexForTimestamp(3999.0).index).isEqualTo(1)
    assertThat(data.findEntryIndexForTimestamp(4000.0)).isEqualTo(EntryIndexSearchResult.NotFound) //exclusive!

  }

  @Test
  fun testSimple() {
    val historyConfiguration = historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(17), "Ref17", HistoryEnum.Boolean)
    }

    val data = DiscreteTimelineChartData(
      arrayOf(
        DiscreteDataEntriesForDataSeries(
          arrayOf(
            DiscreteDataEntry(1001.0, 2002.0, "the label", 1.0),
            DiscreteDataEntry(2002.0, 4003.0, "the label2", 2.0),
          )
        )
      ),
      10.0
    )

    val pair = data.toChunk(historyConfiguration)
    requireNotNull(pair)
    val samplingPeriod = pair.second
    val chunk = pair.first

    assertThat(samplingPeriod).isEqualTo(SamplingPeriod.EveryMillisecond)

    assertThat(chunk.firstTimeStamp()).isEqualTo(1001.0)
    assertThat(chunk.lastTimeStamp()).isEqualTo(4003.0)

    assertThat(chunk.timeStampsCount).isEqualTo(3003)

    if (false) {
      println(chunk.dump())
    }

    assertThat(chunk.timestampCenter(TimestampIndex(199))).isEqualTo(1200.0)
    assertThat(chunk.timestampCenter(TimestampIndex(1100))).isEqualTo(2101.0)
    assertThat(chunk.timestampCenter(TimestampIndex(1101))).isEqualTo(2102.0) //from entry


    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0)).id).isEqualTo(1001)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1)).id).isEqualTo(1001)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1990)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(2000)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(2010)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(3000)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(3001)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(3002))).isEqualTo(ReferenceEntryId.NoValue)
  }
}
