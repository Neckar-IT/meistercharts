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
import com.meistercharts.color.Color
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import com.meistercharts.resources.svg.SvgPaintableProviders

/**
 *
 */
class SvgPaintablesDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("0154a84c-35e4-4217-8e50-633eea02b221")
  override val name: String = "SVG Paintables"
  override val description: String = "Shows SVG paintables using a fill hint."
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          var paintableProvider = SvgPaintableProviders.autoScale

          var width = 25.0
          var height = 25.0

          val config = object {
            var fill: ColorProvider = Color.darkgray
            var stroke: ColorProvider = Color.red
          }

          var anchorDirection = Direction.TopLeft

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.silver)
              gc.strokeLine(0.0, gc.height / 2.0, gc.width, gc.height / 2.0)
              gc.strokeLine(gc.width / 2.0, 0.0, gc.width / 2.0, gc.height)

              gc.translate(gc.width / 2.0, gc.height / 2.0)

              val size = Size(width, height)
              val paintable = paintableProvider.get(size, config.fill, config.stroke, anchorDirection)
              gc.saved {
                paintable.paint(paintingContext, 0.0, 0.0)
              }

              gc.stroke(Color.orangered)
              gc.strokeRect(0.0, 0.0, size.width, size.height)
            }
          })

          this@MeisterchartsDemo.configurable("Fill", config::fill)
          this@MeisterchartsDemo.configurable("Stroke", config::stroke)

          configurableDouble("Image Width", width) {
            max = 200.0

            onChange {
              width = it
              markAsDirty()
            }
          }

          configurableDouble("Image Height", height) {
            max = 200.0

            onChange {
              height = it
              markAsDirty()
            }
          }

          configurableEnum("Anchor Direction", anchorDirection, Direction.entries) {
            onChange {
              anchorDirection = it
              markAsDirty()
            }
          }

          configurableList("Paintable", paintableProvider, SvgPaintableProviders.all()) {
            converter = { paintable ->
              paintable.toString()
            }

            onChange { it ->
              paintableProvider = it
              markAsDirty()
            }
          }
        }
      }

    }
  }
}
