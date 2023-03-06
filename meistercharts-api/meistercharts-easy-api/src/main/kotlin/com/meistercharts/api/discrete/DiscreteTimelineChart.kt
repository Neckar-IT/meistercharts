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

/**
 * Timeline chart that visualizes discrete timelines.
 */
@JsExport
class DiscreteTimelineChart(
  internal val gestalt: DiscreteTimelineChartGestalt,

  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  meisterChart: MeisterChartJS,
) : MeisterChartsApi<DiscreteTimelinechartConfiguration>(meisterChart) {

  init {
    gestalt.applySickDefaults()
  }

  override fun setConfiguration(jsConfiguration: DiscreteTimelinechartConfiguration) {
    gestalt.applyConfiguration(jsConfiguration)
    markAsDirty()
  }
}
