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
package com.meistercharts.demojs

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.demo.descriptors.createCircularChartValues
import com.meistercharts.js.MeisterChartBuilderJS
import com.meistercharts.js.MeisterChartsPlatform
import it.neckar.open.provider.DefaultDoublesProvider
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

/**
 */
class MultiCircularChartsDemo(container: HTMLElement, withLegend: Boolean) {
  init {
    MeisterChartsPlatform.init()

    for (rows in 0 until 20) {
      val rowElement = document.createElement("span")
      container.appendChild(rowElement)

      for (cols in 0 until 5) {
        rowElement.appendChild(
          createCircularChart(withLegend).apply {
            style.width = "400px"
            style.height = "400px"
            style.margin = "5px"
            style.cssFloat = "inline-start"
          }
        )
      }
    }
  }
}


private fun createCircularChart(withLegend: Boolean): HTMLDivElement {
  return MeisterChartBuilderJS("CircularChart")
    .apply {
      zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)
      configure {
        chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

        layers.addClearBackground()

        val valuesProvider = DefaultDoublesProvider(createCircularChartValues(4))

        val layer = CircularChartLayer(valuesProvider)
        layer.style.maxDiameter = 200.0
        layers.addLayer(layer)

        if (withLegend) {
          val legendLayer = CircularChartLegendLayer(valuesProvider)
          layers.addLayer(legendLayer)
        }
        //layers.addLayer(MarkAsDirtyLayer())
      }
    }
    .build().holder
}
