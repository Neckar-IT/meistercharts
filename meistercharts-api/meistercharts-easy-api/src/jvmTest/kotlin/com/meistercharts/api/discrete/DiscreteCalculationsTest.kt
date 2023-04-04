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
    assertThat(DiscreteTimelineChartData(emptyArray()).toChunk(HistoryConfiguration.empty)).isNull()
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
      )
    )

    val pair = data.toChunk(historyConfiguration)
    requireNotNull(pair)
    val samplingPeriod = pair.second
    val chunk = pair.first

    assertThat((4000.0 - 1000.0) / samplingPeriod.distance).isBetween(100.0, 600.0)

    assertThat(samplingPeriod).isEqualTo(SamplingPeriod.EveryTenMillis)

    assertThat(chunk.firstTimeStamp()).isEqualTo(1001.0)
    assertThat(chunk.lastTimeStamp()).isEqualTo(4003.0)

    assertThat(chunk.timeStampsCount).isEqualTo(303)


    println(chunk.dump())

    assertThat(chunk.timestampCenter(TimestampIndex(99))).isEqualTo(1991.0)
    assertThat(chunk.timestampCenter(TimestampIndex(100))).isEqualTo(2001.0)
    assertThat(chunk.timestampCenter(TimestampIndex(101))).isEqualTo(2002.0) //from entry


    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0)).id).isEqualTo(1001)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1)).id).isEqualTo(1001)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(199)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(200)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(201)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(300)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(301)).id).isEqualTo(1002)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(302))).isEqualTo(ReferenceEntryId.NoValue)
  }
}