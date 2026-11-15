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
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.paintable.DebugPaintable
import com.meistercharts.canvas.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeStyle
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.http.Url

/**
 * Reproduces a bug in the JS implementation related to paintables and bounding boxes
 */
class PaintableBugInJsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("3c6a11fb-a708-41fa-9be5-7579b82391bc")
  override val name: String = "Paintable Bug JS"

  override val variabilityType: VariabilityType = VariabilityType.Stable
  override val quality: DemoQuality = DemoQuality.High

  override val category: DemoCategory = DemoCategory.BugHunting

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        val paintables: List<Paintable> = listOf(
          LocalResourcePaintable(Url.relative("elevator/elevator.png")),
          DebugPaintable(),
        )


        val config = object {
          var x = 500.0
          var y = 550.0
          var width = 350.0
          var height = 480.0
          var anchorDirection = Direction.Center
          var objectFit = ObjectFit.Fill
          var paintable: Paintable = paintables[0]
        }

        configurable("X", config::x) {
          max = 1000.0
        }
        configurable("Y", config::y) {
          max = 1000.0
        }
        configurable("Width", config::width) {
          max = 1000.0
        }
        configurable("Height", config::height) {
          max = 1000.0
        }

        configurable("Anchor Direction", config::anchorDirection)
        configurable("Object Fit", config::objectFit)
        configurableListWithProperty("Paintable", config::paintable, paintables) {
          converter {
            when (it) {
              is LocalResourcePaintable -> "LocalResourcePaintable: " + it.relativePath.toString()
              is DebugPaintable -> "Debug Paintable"
              else -> "Unknown: $it"
            }
          }

        }

        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.saved {
                config.paintable.paintInBoundingBox(
                  paintingContext = paintingContext,
                  x = config.x,
                  y = config.y,
                  width = config.width,
                  height = config.height,
                  anchorDirection = config.anchorDirection,
                  objectFit = config.objectFit
                )
              }

              gc.paintLocation(config.x, config.y)

              gc.strokeStyle(Color.red)

              val box = Rectangle.fromOrigin(Coordinates(config.x, config.y), Size(config.width, config.height), config.anchorDirection)
              gc.strokeRect(box)
            }
          })
        }
      }
    }
  }
}
