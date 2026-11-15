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
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableSize
import com.meistercharts.resources.Icons
import com.meistercharts.resources.svg.PathPaintable
import it.neckar.geometry.Size
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.unit.other.px

/**
 */
class IconsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("d25786e5-a21a-47f4-b6b0-d5577ff06418")
  override val name: String = "Icons: All available icons"

  override val variabilityType: VariabilityType = VariabilityType.Stable
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {

          val config = object {
            var size = Size.PX_24
            var fill: ColorProvider = Color.lightgray

            /**
             * The gap between the icons
             */
            var gap: @px Double = 10.0
          }

          configurable("Size", config::size)
          configurable("Fill", config::fill)

          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              //ATTENTION! THIS IS SLOW! DO NOT COPY THIS CODE (enough for a demo, *not* for production)
              val icons: List<PathPaintable> = Icons.all(size = config.size, config.fill)

              val gc = paintingContext.gc

              @px val netWidth = gc.width - 30.0
              @px val netHeight = gc.height - 30.0

              val rowSize = (netWidth / (config.size.width + config.gap)).toInt()
              val rowCount = (netHeight / (config.size.height + config.gap)).toInt()


              //Translate to center of first icon
              gc.translate(config.gap + config.size.width / 2.0, config.gap + config.size.height / 2.0)

              icons.fastForEachIndexed { index, paintable ->
                val row = index / rowSize
                val column = index % rowSize

                gc.saved {
                  //Translate to the position of the icon
                  gc.translate(column * (config.size.width + config.gap), row * (config.size.height + config.gap))

                  gc.saved {
                    paintable.paint(paintingContext)
                  }

                  if (gc.debug[DebugFeature.ShowAnchors]) {
                    gc.stroke(Color.darkgray)
                    gc.paintMark()
                  }

                  if (gc.debug[DebugFeature.ShowBounds]) {
                    gc.stroke(Color.lightblue)
                    gc.lineWidth = 1.0
                    gc.strokeRect(paintable.alignmentPoint, size = config.size)
                  }
                }
              }
            }
          })
        }
      }
    }
  }
}
