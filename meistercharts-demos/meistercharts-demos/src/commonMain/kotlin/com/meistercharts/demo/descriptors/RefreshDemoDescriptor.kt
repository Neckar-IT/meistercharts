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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.RefreshListener
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.formatting.dateTimeFormatWithMillis

/**
 *
 */
class RefreshDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Refresh Demo"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(MyRefreshDebugLayer().also {
            chartSupport.onRefresh(it)
          })
        }
      }
    }
  }
}

class MyRefreshDebugLayer : AbstractLayer(), RefreshListener {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.paintTextBox(
      listOf(
        "now: ${paintingContext.frameTimestamp}",
        "now delta: ${paintingContext.frameTimestampDelta}",
        "now formatted: ${dateTimeFormatWithMillis.format(paintingContext.frameTimestamp, paintingContext.i18nConfiguration)}",
        "Last refresh time: $lastRefreshTime",
        "Last refresh delta: $lastRefreshDelta",
        "Last refresh delta formatted: ${dateTimeFormatWithMillis.format(lastRefreshTime, paintingContext.i18nConfiguration)}"
      ),
      Direction.TopLeft
    )
  }

  var lastRefreshTime: Double = 0.0
  var lastRefreshDelta: Double = Double.NaN

  override fun refresh(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
    lastRefreshTime = frameTimestamp
    lastRefreshDelta = refreshDelta
    chartSupport.markAsDirty()
  }
}
