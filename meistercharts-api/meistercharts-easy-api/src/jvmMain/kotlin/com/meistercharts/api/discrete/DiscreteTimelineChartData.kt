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

  actual val defaultEntryDuration: @ms Double

  companion object {
    operator fun invoke(seriesData: Array<DiscreteDataEntriesForDataSeries>, defaultEntryDuration: @ms Double): DiscreteTimelineChartDataImpl {
      return DiscreteTimelineChartDataImpl(seriesData, defaultEntryDuration)
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
  actual val label: String?
  actual val status: Double?

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
  override val defaultEntryDuration: @ms Double,
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
