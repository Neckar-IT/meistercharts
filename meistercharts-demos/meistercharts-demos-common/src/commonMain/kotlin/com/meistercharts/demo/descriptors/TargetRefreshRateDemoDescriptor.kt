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
import com.meistercharts.algorithms.layers.debug.FramesPerSecondLayer
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.debug.PaintCounterLayer
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.fill
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.style.Palette.getChartColor

/**
 */
class TargetRefreshRateDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5f69a52f-bae8-4443-b1bb-3014a213e043")
  override val name: String = "Target refresh rate"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(MyNewColorOnRepaintLayer())

          layers.addText { _, _ ->
            "Hello World"
          }

          layers.addLayer(PaintCounterLayer())
          layers.addLayer(MarkAsDirtyLayer())
          layers.addLayer(FramesPerSecondLayer())

          //Enable recording of paint statistics - required for
          recordPaintStatistics = true
          configurableListWithProperty("Refresh rate", this.chartSupport::targetRenderRate, TargetRefreshRate.predefined) {
            converter {
              "${it.refreshRate} FPS"
            }
          }
        }
      }
    }
  }
}

private class MyNewColorOnRepaintLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  var colorIndex = 0

  override fun paint(paintingContext: LayerPaintingContext) {

    val gc = paintingContext.gc
    gc.fill(getChartColor(colorIndex))
    colorIndex++


    gc.fillRect(0.0, 0.0, 100.0, 100.0)

  }
}
