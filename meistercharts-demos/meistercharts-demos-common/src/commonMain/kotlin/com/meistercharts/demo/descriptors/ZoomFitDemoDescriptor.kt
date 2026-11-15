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
@file:Suppress("RETURN_VALUE_NOT_USED") // Demo-scope subscriptions tied to the demo lifetime

package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.ZoomAndTranslationDebugLayer
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoConfiguration
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section

/**
 *
 */
class ZoomFitDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("24d51c40-5be0-4862-8a37-12bf5694a0b2")
  override val name: String = "Zoom/Pan Fit"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(ZoomAndTranslationDebugLayer())

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {

            }
          })

          section("Fit X")
          declare {
            fitXButton(it.chartSupport, 0.0, 1.0)
            fitXButton(it.chartSupport, 1.0, 2.0)
            fitXButton(it.chartSupport, 0.0, 2.0)
            fitXButton(it.chartSupport, 0.5, 1.0)
            fitXButton(it.chartSupport, 0.0, 0.5)
            fitXButton(it.chartSupport, -0.5, 0.5)
            fitXButton(it.chartSupport, -1.0, 0.0)
          }

          @DomainRelative var beginX = 0.0
          @DomainRelative var endX = 1.0

          configurableDouble("Begin X", beginX) {
            min = -2.0
            max = 2.0

            onChange {
              beginX = it
              chartSupport.zoomAndTranslationSupport.fitX(beginX, endX, reason = UpdateReason.UserInteraction)
            }
          }
          configurableDouble("End X", endX) {
            min = -2.0
            max = 2.0

            onChange {
              endX = it
              chartSupport.zoomAndTranslationSupport.fitX(beginX, endX, reason = UpdateReason.UserInteraction)
            }
          }
          section("Fit Y")
          declare {
            fitYButton(it.chartSupport, 0.0, 1.0)
            fitYButton(it.chartSupport, 1.0, 2.0)
            fitYButton(it.chartSupport, 0.0, 2.0)
            fitYButton(it.chartSupport, 0.5, 1.0)
            fitYButton(it.chartSupport, 0.0, 0.5)
            fitYButton(it.chartSupport, -0.5, 0.5)
            fitYButton(it.chartSupport, -1.0, 0.0)
          }

          @DomainRelative var beginY = 0.0
          @DomainRelative var endY = 1.0

          configurableDouble("Begin Y", beginY) {
            min = -2.0
            max = 2.0

            onChange {
              beginY = it
              chartSupport.zoomAndTranslationSupport.fitY(beginY, endY, reason = UpdateReason.UserInteraction)
            }
          }
          configurableDouble("End Y", endY) {
            min = -2.0
            max = 2.0

            onChange {
              endY = it
              chartSupport.zoomAndTranslationSupport.fitY(beginY, endY, reason = UpdateReason.UserInteraction)
            }
          }

        }
      }
    }
  }
}

private fun DemoConfiguration.fitXButton(chartSupport: ChartSupport, begin: Double, end: Double) {
  button("$begin / $end") {
    if (chartSupport.currentChartState.axisOrientationX.axisInverted) {
      chartSupport.zoomAndTranslationSupport.fitX(end, begin, reason = UpdateReason.UserInteraction)
    } else {
      chartSupport.zoomAndTranslationSupport.fitX(begin, end, reason = UpdateReason.UserInteraction)
    }
  }
}

private fun DemoConfiguration.fitYButton(chartSupport: ChartSupport, begin: Double, end: Double) {
  button("$begin / $end") {
    if (chartSupport.currentChartState.axisOrientationY.axisInverted) {
      chartSupport.zoomAndTranslationSupport.fitY(end, begin, reason = UpdateReason.UserInteraction)
    } else {
      chartSupport.zoomAndTranslationSupport.fitY(begin, end, reason = UpdateReason.UserInteraction)
    }
  }
}
