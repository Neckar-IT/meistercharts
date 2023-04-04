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
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Size
import com.meistercharts.charts.lizergy.planning.UnusableArea
import com.meistercharts.charts.lizergy.planning.UnusableAreaPainter
import it.neckar.open.kotlin.lang.enumEntries

/**
 *
 */
class UnusableAreaPaintableDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Unusable area paintable"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addLayer(FillBackgroundLayer() {
            dark()
          })

          val paintable = UnusableAreaPainter()

          val unusableArea = UnusableArea(size = Size(3000.0, 1200.0)).apply {
            description = "The UnusableArea"
          }

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var mode: UnusableAreaPainter.Mode = UnusableAreaPainter.Mode.Default

            var width: Double = 100.0
            var height: Double = 80.0

            override fun paint(paintingContext: LayerPaintingContext) {
              paintingContext.gc.translate(40.0, 40.0)
              //paintable.paint(paintingContext, 0.0, 0.0, width, height, unusableArea, mode)
            }
          }
          layers.addLayer(layer)

          configurableDouble(layer::width) {
            max = 1000.0
          }
          configurableDouble(layer::height) {
            max = 1000.0
          }

          configurableEnum("Mode", layer::mode, enumEntries())
        }
      }
    }
  }
}
