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
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipLayer
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoxStyle
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnumProvider
import com.meistercharts.demo.configurableNosePosition
import com.meistercharts.demo.section
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.MultiProvider1

/**
 */
class BalloonTooltipLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Balloon Tooltips Layer"

  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {

          val locations = listOf(
            Coordinates(110.0, 110.0),
            Coordinates(310.0, 310.0),
            Coordinates(510.0, 510.0),
          )

          val markerLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              locations.fastForEach {
                gc.paintMark(it)
              }
            }
          }

          val coordinates: CoordinatesProvider1<LayerPaintingContext> = object : CoordinatesProvider1<LayerPaintingContext> {
            override fun size(param1: LayerPaintingContext): Int {
              return 3
            }

            override fun xAt(index: Int, param1: LayerPaintingContext): Double {
              return locations[index].x
            }

            override fun yAt(index: Int, param1: LayerPaintingContext): Double {
              return locations[index].y
            }
          }
          val tooltipsLayer = BalloonTooltipLayer(
            BalloonTooltipLayer.Configuration(
              coordinates = coordinates,
              tooltipContent = MultiProvider1 { index, param1 ->
                RectanglePaintable(Size.PX_24, Color.blue)
              })
          ) {}

          tooltipsLayer.tooltipPainter.configuration.noseSide

          layers.addClearBackground()
          layers.addBackgroundChecker()
          layers.addLayer(markerLayer)
          layers.addLayer(tooltipsLayer)


          configurableEnumProvider("Side", property = tooltipsLayer.tooltipPainter.configuration::noseSide)

          section("Nose Position")
          configurableNosePosition(tooltipsLayer.tooltipPainter)

          configurableDouble("Nose Width", tooltipsLayer.tooltipPainter.configuration::noseWidth) {
            max = 20.0
          }
          configurableDouble("Nose Length", tooltipsLayer.tooltipPainter.configuration::noseLength) {
            max = 20.0
          }

          configurableBoxStyle("Box Style", tooltipsLayer.tooltipPainter.configuration::boxStyle)
        }
      }
    }
  }
}
