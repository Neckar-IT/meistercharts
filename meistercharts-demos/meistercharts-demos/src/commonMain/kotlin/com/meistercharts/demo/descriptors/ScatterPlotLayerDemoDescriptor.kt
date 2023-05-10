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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addShowZoomLevel
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.scatterplot.ScatterPlotLayer
import com.meistercharts.charts.ScatterPlotGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

class ScatterPlotLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Scatter Plot Layer Demo"
  override val description: String = "Scatter Plot Layer Demo"
  override val category: DemoCategory = DemoCategory.Layers


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        val data = ScatterPlotGestalt.createDefaultData()

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val scatterPlotLayer = ScatterPlotLayer(data)
          layers.addLayer(scatterPlotLayer)

          layers.addShowZoomLevel()
        }
      }
    }
  }
}
