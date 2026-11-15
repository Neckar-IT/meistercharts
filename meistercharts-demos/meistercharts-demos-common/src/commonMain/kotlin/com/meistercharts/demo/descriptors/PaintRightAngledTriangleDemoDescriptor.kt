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
import com.meistercharts.canvas.LocationType
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section
import it.neckar.geometry.RightTriangleType

class PaintRightAngledTriangleDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("295caff2-36c0-45ed-97fd-59d472d74745")
  override val name: String = "Triangles Right Angled"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content
            var x: Double = 100.0
            var y: Double = 100.0

            var width = 100.0
            var height = 150.0

            var triangleType = RightTriangleType.MissingCornerInFirstQuadrant

            var xLocationType: LocationType = LocationType.Origin
            var yLocationType: LocationType = LocationType.Origin
            var lineWidth: Double = 3.0


            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.lineWidth = lineWidth
              gc.strokeTriangleRightAngled(x, y, width, height, triangleType, xLocationType, yLocationType)

              gc.lineWidth = 1.0
              gc.paintMark(x, y, color = Color.orange())
            }
          }

          layers.addLayer(layer)

          section("Simple Triangle")

          section("Advanced Triangle")
          configurable("x", layer::x) {
            max = 1000.0
          }
          configurable("y", layer::y) {
            max = 1000.0
          }
          configurable("width", layer::width) {
            max = 400.0
          }
          configurable("height", layer::height) {
            max = 400.0
          }

          configurable("Triangle Type", layer::triangleType)
          configurable("X location type", layer::xLocationType)
          configurable("y location type", layer::yLocationType)

          configurable("Line Width", layer::lineWidth) {
            max = 10.0
          }
        }
      }
    }
  }
}
