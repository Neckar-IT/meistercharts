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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.ChartRenderLoopListener
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.unit.si.ms

/**
 *
 */
class RefreshDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("a7ecb19d-96b0-49d2-852b-93a0d87859c9")
  override val name: String = "Refresh Demo"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(MyRenderDebugLayerLoop().also {
            chartSupport.onRender(it)
          })
        }
      }
    }
  }
}

class MyRenderDebugLayerLoop : AbstractLayer(), ChartRenderLoopListener {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.paintTextBox(
      listOf(
        "now: ${paintingContext.frameTimestamp}",
        "frame delta: ${paintingContext.frameDelta}",
        "now formatted: ${dateTimeFormatWithMillis.format(paintingContext.frameTimestamp, paintingContext.i18nConfiguration)}",
        "Last refresh time: $lastRefreshTime",
        "Last refresh delta: $lastRefreshDelta",
        "Last refresh delta formatted: ${dateTimeFormatWithMillis.format(lastRefreshTime, paintingContext.i18nConfiguration)}"
      ),
      Direction.TopLeft
    )
  }

  var lastRefreshTime: Double = 0.0
  @ms
  var lastRefreshDelta: Double = Double.NaN

  @ms
  private var lastRefreshMonotonicMillis: Double = Double.NaN

  override fun render(chartSupport: ChartSupport, frameTimestamp: Double, frameMonotonicMillis: @ms Double) {
    lastRefreshTime = frameTimestamp
    lastRefreshDelta = frameMonotonicMillis - lastRefreshMonotonicMillis
    lastRefreshMonotonicMillis = frameMonotonicMillis
    chartSupport.markAsDirty(DirtyReason.DataUpdated)
  }
}
