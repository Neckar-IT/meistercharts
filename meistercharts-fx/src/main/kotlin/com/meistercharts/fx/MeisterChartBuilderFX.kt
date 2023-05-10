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
package com.meistercharts.fx

import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.charts.ChartGestalt
import com.meistercharts.charts.ChartId

/**
 * A builder for [MeisterChartFX]
 */
class MeisterChartBuilderFX
@JvmOverloads constructor(
  description: String,
  chartId: ChartId = ChartId.next(),
) : MeisterChartBuilder(description, chartId = chartId) {
  /**
   * Builds the chart
   */
  override fun build(): MeisterChartFX {
    return super.build() as MeisterChartFX
  }

  companion object {
    /**
     * Creates a new chart builder - calls <MeisterChartsPlatform.init()>
     */
    @JvmOverloads
    @JvmStatic
    fun create(description: String, chartId: ChartId = ChartId.next()): MeisterChartBuilderFX {
      MeisterChartsPlatform.init()
      return MeisterChartBuilderFX(description, chartId)
    }
  }
}

/**
 * Creates a new node from a gestalt
 */
fun ChartGestalt.build(description: String): MeisterChartFX {
  return MeisterChartBuilderFX(description)
    .also {
      configure(it)
    }
    .build()
}

fun MeisterChartFX.Companion.create(description: String, configure: MeisterChartBuilderFX.() -> Unit): MeisterChartFX {
  return MeisterChartBuilderFX(description)
    .also(configure)
    .build()
}
