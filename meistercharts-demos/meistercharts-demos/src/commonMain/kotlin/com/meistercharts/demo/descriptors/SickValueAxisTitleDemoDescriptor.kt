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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.annotations.Window
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.BooleanProvider

/**
 *
 */
@Deprecated("currently not used")
class SickValueAxisTitleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "SICK Value Axis Title (deprecated)"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val withContentViewportGestalt = FitContentInViewportGestalt(Insets.of(50.0, 150.0, 40.0, 40.0)).also {
          it.configure(this)
        }

        configure {
          layers.addClearBackground()

          val yAxis = ValueAxisLayer("Bar", ValueRange.linear(0.0, 243.0)) {
            side = Side.Left
            tickOrientation = Vicinity.Outside
            titleVisible = BooleanProvider.False
            size = withContentViewportGestalt.contentViewportMargin.left
          }
          layers.addLayer(yAxis)

          val xAxis = ValueAxisLayer("Meters", ValueRange.linear(0.0, 100.0)) {
            side = Side.Bottom
            tickOrientation = Vicinity.Outside
            titleVisible = BooleanProvider.False
            size = withContentViewportGestalt.contentViewportMargin.bottom
          }

          layers.addLayer(xAxis)

          layers.addLayer(SickValueAxisLabelLayer() {
            val style = xAxis.style

            val y = it.gc.height - style.margin.bottom - style.size + style.axisLineWidth + style.tickLength + style.tickLabelGap
            Coordinates(xAxis.paintingVariables().axisEnd, y)
          })


          configurableDouble("size", xAxis.style::size) {
            max = 200.0
          }
          configurableDouble("axisLineWidth", xAxis.style::axisLineWidth) {
            max = 20.0
          }
          configurableDouble("tickLength", xAxis.style::tickLength) {
            max = 20.0
          }
          configurableDouble("tickLabelGap", xAxis.style::tickLabelGap) {
            max = 20.0
          }
        }
      }
    }
  }
}

@Deprecated("currently not used")
class SickValueAxisLabelLayer(val locationProvider: (paintingContext: LayerPaintingContext) -> @Window Coordinates) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    //Paint the title
    val gc = paintingContext.gc

    locationProvider(paintingContext).let { location ->
      gc.translate(location.x, location.y)
    }

    gc.fillText("Axis Title", 0.0, 0.0, Direction.TopLeft)
  }
}
