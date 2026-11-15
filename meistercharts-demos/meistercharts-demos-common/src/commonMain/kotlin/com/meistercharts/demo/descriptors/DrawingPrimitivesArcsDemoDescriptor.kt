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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.geometry.RotationDirection
import kotlin.math.PI

/**
 */
class DrawingPrimitivesArcsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("3ee0a8c9-37ee-4c81-b117-38e6e8cb905e")
  override val name: String = "Drawing Primitives: Arcs"

  //language=HTML
  override val description: String = "Shows how to draw arcs"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()


          var radius = 150.0
          var startAngle = 0.0
          var arcExtent = 1.5 * PI
          var arcType = ArcType.Open

          var rotationDirection = RotationDirection.Clockwise

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translateToCenter()
              val extendWithRotationDirection = rotationDirection.toPlattformRotationDirection(arcExtent)

              gc.fill(Color.silver)
              gc.fillArcCenter(0.0, 0.0, radius, startAngle, extendWithRotationDirection, arcType)
              gc.stroke(Color.orangered)
              gc.strokeArcCenter(0.0, 0.0, radius, startAngle, extendWithRotationDirection, arcType)

              gc.paintMark(color = Color.orangered())
            }
          })


          configurableEnum("Rotation direction", rotationDirection, kotlin.enums.enumEntries<RotationDirection>().toList()) {
            onChange {
              rotationDirection = it
              markAsDirty()
            }
          }

          configurableDouble("radius", radius) {
            max = 500.0

            onChange {
              radius = it
              markAsDirty()
            }
          }

          configurableDouble("startAngle", startAngle) {
            max = 10.0

            onChange {
              startAngle = it
              markAsDirty()
            }
          }

          configurableDouble("arcExtent", arcExtent) {
            max = 10.0

            onChange {
              arcExtent = it
              markAsDirty()
            }
          }

          configurableEnum("arcType", arcType, ArcType.entries) {
            onChange {
              arcType = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
