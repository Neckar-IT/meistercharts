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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.Exact
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.layout.LayoutMode
import com.meistercharts.algorithms.layout.Rounded
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableListWithProperty
import it.neckar.open.kotlin.lang.fastFor

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class EquisizedBoxLayoutDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("212f2737-c192-43df-8009-b92d6dfbfab3")
  override val name: String = "Box layout - equisized"
  override val description: String = "Visualizes the equisized box layout"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()


          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var numberOfBoxes: Int = 7
            var layoutDirection = LayoutDirection.LeftToRight
            var minBoxSize = 10.0
            var maxBoxSize = 40.0
            var gapSize = 2.0
            var layoutMode: LayoutMode = Exact


            override fun paint(paintingContext: LayerPaintingContext) {
              val chartCalculator = paintingContext.chartCalculator
              val chartState = paintingContext.chartState

              val layout = BoxLayoutCalculator.layout(
                chartState.contentAreaWidth, numberOfBoxes, layoutDirection = layoutDirection,
                minBoxSize = minBoxSize,
                maxBoxSize = maxBoxSize,
                gapSize = gapSize,
                layoutMode = layoutMode
              )

              val gc = paintingContext.gc

              numberOfBoxes.fastFor { boxIndexAsInt ->
                val boxIndex = BoxIndex(boxIndexAsInt)

                val start = layout.calculateStart(boxIndex)
                val end = layout.calculateEnd(boxIndex)
                val center = layout.calculateCenter(boxIndex)

                gc.stroke(Color.red)
                gc.strokeRectCoordinates(start, 5.0, end, 40.0)

                gc.stroke(Color.green)
                gc.strokeLine(center, 5.0, center, 40.0)
              }
            }
          }

          layers.addLayer(layer)

          configurable("Number of boxes", layer::numberOfBoxes) {
            max = 200
          }
          configurable("Layout direction", layer::layoutDirection)
          configurable("Min box size", layer::minBoxSize) {
            max = 200.0
          }
          configurable("Max box size", layer::maxBoxSize) {
            max = 200.0
          }
          configurable("Gap size", layer::gapSize) {
            max = 200.0
          }
          configurableListWithProperty("Layout Mode", layer::layoutMode, listOf(Exact, Rounded)) {
            converter {
              when (it) {
                Exact -> "Exact"
                Rounded -> "Rounded"
                else -> it.toString()
              }
            }
          }
        }
      }
    }
  }
}
