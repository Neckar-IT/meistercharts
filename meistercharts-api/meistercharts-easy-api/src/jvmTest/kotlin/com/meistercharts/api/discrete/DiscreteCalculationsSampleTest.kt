package com.meistercharts.api.discrete

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.RecordingType
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.formatting.formatUtc
import org.junit.jupiter.api.Test

class DiscreteCalculationsSampleTest {
  val seriesData: Array<DiscreteDataEntriesForDataSeries> = arrayOf(
    DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(1680598565461.0, 1680598566461.0, "Stumbled over switch", status = 0.0),
        DiscreteDataEntry(1680598566461.0, 1680598567461.0, "Turned switch off", status = 1.0),
      )
    ),
    DiscreteDataEntriesForDataSeries(
      arrayOf(
        DiscreteDataEntry(1680598565961.0, 1680598566961.0, "Everything is fine", status = 3.0),
        DiscreteDataEntry(1680598566961.0, 1680598567961.0, "Fuel is empty", status = 0.0),
      )
    ),
  )

  val data: DiscreteTimelineChartDataImpl = DiscreteTimelineChartData(
    seriesData
  )

  @Test
  fun testFirstLast() {
    assertThat(data.findFirstStart()).isEqualTo(1680598565461.0)
    assertThat(data.findLastEnd()).isEqualTo(1680598567961.0)
  }

  @Test
  fun testExample() {
    val series0 = data.series[0]
    val series1 = data.series[1]

    assertThat(series0.entries.first().label).isEqualTo("Stumbled over switch")
    assertThat(series0.entries.first().start.formatUtc()).isEqualTo("2023-04-04T08:56:05.461")

    assertThat(series0.entries.last().label).isEqualTo("Turned switch off")
    assertThat(series0.entries.last().end.formatUtc()).isEqualTo("2023-04-04T08:56:07.461")

    assertThat(series1.entries.first().start.formatUtc()).isEqualTo("2023-04-04T08:56:05.961")
    assertThat(series1.entries.last().end.formatUtc()).isEqualTo("2023-04-04T08:56:07.961")

    val pair = data.toChunk(historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(17), "A", HistoryEnum.Active)
      referenceEntryDataSeries(DataSeriesId(18), "B", HistoryEnum.Active)
    })
    requireNotNull(pair)

    val chunk = pair.first
    val samplingPeriod = pair.second

    assertThat(samplingPeriod).isEqualTo(SamplingPeriod.EveryTenMillis)

    //Verify distance is not too large
    chunk.timeStamps.fastForEachIndexed { index, timestamp ->
      if (index == 0) {
        return@fastForEachIndexed
      }

      val previous = chunk.timeStamps[index - 1]
      val delta = timestamp - previous

      assertThat(delta).isLessThanOrEqualTo(SamplingPeriod.EveryTenMillis.distance)
    }

    if (false) {
      println(chunk.dump())
    }

    assertThat(chunk.recordingType).isEqualTo(RecordingType.Measured)

    assertThat(chunk.start.formatUtc()).isEqualTo("2023-04-04T08:56:05.461")
    assertThat(chunk.end.formatUtc()).isEqualTo("2023-04-04T08:56:07.961")
  }
}
