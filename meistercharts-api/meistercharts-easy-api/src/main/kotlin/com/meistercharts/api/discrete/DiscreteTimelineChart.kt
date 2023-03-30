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

import com.meistercharts.api.MeisterChartsApi
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.js.MeisterChartJS
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms

/**
 * Timeline chart that visualizes discrete timelines.
 */
@JsExport
class DiscreteTimelineChart internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: DiscreteTimelineChartGestalt,

  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  meisterChart: MeisterChartJS,
) : MeisterChartsApi<DiscreteTimelineChartConfiguration>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setConfiguration(jsConfiguration: DiscreteTimelineChartConfiguration) {
    gestalt.applyConfiguration(jsConfiguration)
    markAsDirty()
  }

  fun setData(data: DiscreteTimelineChartData) {
    //clear history
    //set new history configuration from data
    //set data
  }
}

@JsExport
class DiscreteTimelineChartData(
  /**
   * Index corresponds to the data series index
   */
  val series: Array<DiscreteDataEntriesForDataSeries>,
)

@JsExport
class DiscreteDataEntriesForDataSeries(
  /**
   * Contains all entries for this data series.
   * Must not overlap!
   */
  val entries: Array<@Sorted(by = "from") DiscreteDataEntry>,
)

@JsExport
class DiscreteDataEntry(
  val from: @ms Double,
  val to: @ms Double,
  val label: String,
  val status: Double, //must be double since JS does not support Int
)

