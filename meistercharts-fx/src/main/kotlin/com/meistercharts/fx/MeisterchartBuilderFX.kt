/*
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

import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.charts.ChartGestalt
import com.meistercharts.charts.ChartId

/**
 * A builder for [MeisterchartFX]
 */
class MeisterchartBuilderFX
@JvmOverloads constructor(
  description: String,
  chartId: ChartId = ChartId.next(),
) : MeisterchartBuilder(description, chartId = chartId) {
  /**
   * Builds the chart
   */
  override fun build(): MeisterchartFX {
    return super.build() as MeisterchartFX
  }

  companion object {
    /**
     * Creates a new chart builder - calls <MeisterChartsPlatform.init()>
     */
    @JvmOverloads
    @JvmStatic
    fun create(description: String, chartId: ChartId = ChartId.next()): MeisterchartBuilderFX {
      MeisterChartsPlatform.init()
      return MeisterchartBuilderFX(description, chartId)
    }
  }
}

/**
 * Creates a new node from a gestalt
 */
fun ChartGestalt.build(description: String): MeisterchartFX {
  return MeisterchartBuilderFX(description)
    .also {
      configure(it)
    }
    .build()
}

fun MeisterchartFX.Companion.create(description: String, configure: MeisterchartBuilderFX.() -> Unit): MeisterchartFX {
  return MeisterchartBuilderFX(description)
    .also(configure)
    .build()
}
