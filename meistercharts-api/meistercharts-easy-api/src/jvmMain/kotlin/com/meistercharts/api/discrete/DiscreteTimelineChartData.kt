package com.meistercharts.api.discrete

import it.neckar.open.unit.si.ms

/**
 * Contains the data for the discrete timeline chart
 */
actual interface DiscreteTimelineChartData {
  /**
   * Index corresponds to the data series index.
   * Contains one entry for each data series.
   */
  actual val series: Array<DiscreteDataEntriesForDataSeries>

  companion object {
    operator fun invoke(seriesData: Array<DiscreteDataEntriesForDataSeries>): DiscreteTimelineChartDataImpl {
      return DiscreteTimelineChartDataImpl(seriesData)
    }
  }
}

/**
 * Contains the entries for a single discrete data series
 */
actual interface DiscreteDataEntriesForDataSeries {
  /**
   * Contains all entries for this data series.
   * Must not overlap!
   */
  actual val entries: Array<DiscreteDataEntry>

  companion object {
    operator fun invoke(entries: Array<DiscreteDataEntry>): DiscreteDataEntriesForDataSeries {
      return DiscreteDataEntriesForDataSeriesImpl(entries)
    }
  }
}

actual interface DiscreteDataEntry {
  actual val start: Double
  actual val end: Double
  actual val label: String
  actual val status: Double

  companion object {
    operator fun invoke(
      start: @ms Double,
      end: @ms Double,
      label: String,
      status: Double,
    ): DiscreteDataEntry {
      return DiscreteDataEntryImpl(start, end, label, status)
    }
  }
}

class DiscreteTimelineChartDataImpl(
  override val series: Array<DiscreteDataEntriesForDataSeries>,
) : DiscreteTimelineChartData {
}

class DiscreteDataEntriesForDataSeriesImpl(
  override val entries: Array<DiscreteDataEntry>,
) : DiscreteDataEntriesForDataSeries {
}

data class DiscreteDataEntryImpl(
  override val start: @ms Double,
  override val end: @ms Double,
  override val label: String,
  override val status: Double,
) : DiscreteDataEntry {
}
