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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.time.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.color.Color
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import it.neckar.geometry.Direction

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class TranslateOverTimeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("feb495b4-0a70-4e5c-873a-c407930c9542")
  override val name: String = "Translate over time service"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(MyTranslateOverTimeDebugLayer())

          chartSupport.translateOverTime.contentAreaTimeRangeX = TimeRange.oneMinuteUntilNow()
          configurable("Animated", chartSupport.translateOverTime::animated)
        }
      }
    }
  }
}


private class MyTranslateOverTimeDebugLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    val translateOverTime = paintingContext.chartSupport.translateOverTime
    val timeRange = translateOverTime.contentAreaTimeRangeX ?: return

    gc.fillText("Time range: ${timeRange.format()}", 10.0, 10.0, Direction.TopLeft)

    @TimeRelative val relative = timeRange.time2relative(paintingContext.frameTimestamp)
    @Window val nowX = chartCalculator.domainRelative2windowX(relative)

    gc.stroke(Color.red)
    gc.strokeLine(nowX, 0.0, nowX, gc.height)

  }
}
