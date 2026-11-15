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


import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColor
import com.meistercharts.resources.svg.SvgPaths
import com.meistercharts.svg.SVGPathParser
import kotlin.uuid.Uuid

/**
 *
 */
class SvgPathsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("086b0911-f5c8-47b0-9dd4-6be7de315fe6")
  override val name: String = "SVG Path on canvas"
  override val description: String = "Paints the parsed SVG paths"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      val svgPaths = buildList {
        addAll(SvgPaths.all)
      }

      val paths: List<Path> = svgPaths.map {
        SVGPathParser.from(it).parse()
      }

      meistercharts {
        configure {
          layers.addClearBackground()

          var fill: Color = Color.darkgray()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.scale(4.0, 4.0)
              gc.fill(fill)
              paths
                .chunked(7)
                .forEach { pathsForLine ->
                  gc.saved {
                    pathsForLine.forEach { path ->
                      gc.translate(26.0, 0.0)
                      gc.fill(path)
                    }
                  }

                  gc.translate(0.0, 26.0)
                }
            }
          })

          configurableColor("fill", fill) {
            onChange {
              fill = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
