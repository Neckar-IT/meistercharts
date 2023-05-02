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
package com.meistercharts.js

import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.charts.ChartId

/**
 * A builder for [MeisterChartJS]
 */
class MeisterChartBuilderJS(
  description: String,
  chartId: ChartId = ChartId.next(),
) : MeisterChartBuilder(description, chartId = chartId) {
  init {
    //Enforce a repaint on resize
    //The HTML5 canvas is cleared on resize - therefore a repaint is necessary to avoid flickering to white
    configure {
      chartSupport.canvas.sizeProperty.consume {
        chartSupport.markAsDirty()
      }
    }
  }

  override fun build(): MeisterChartJS {
    return (super.build() as MeisterChartJS)
  }

  companion object {
    /**
     * Creates a new chart builder - calls <MeisterChartsPlatform.init()>
     */
    fun create(description: String): MeisterChartBuilderJS {
      return MeisterChartBuilderJS(description)
    }
  }
}
