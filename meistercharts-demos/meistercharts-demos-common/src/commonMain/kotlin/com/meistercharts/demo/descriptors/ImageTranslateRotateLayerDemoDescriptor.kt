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
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.PaintableTranslateRotateLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addShowZoomLevel
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import com.meistercharts.resources.Icons

class ImageTranslateRotateLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("99c20854-e1ef-470a-ae04-e057275b45c5")

  override val name: String = "Image Translate Rotate Layer"
  override val description: String = "Translates and rotates an image via a paintable layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        val image = Icons.autoScale(Size.PX_120)

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          var angle = 17.0
          @DomainRelative var x = 0.3
          @DomainRelative var y = 0.4

          val layer = PaintableTranslateRotateLayer(
            angle = { angle },
            image = { image },
            x = { x },
            y = { y }
          ) {
            this.direction = Direction.Center
          }

          layers.addLayer(layer)
          layers.addShowZoomLevel()

          configurableDouble("angle", angle) {
            min = -7.0
            max = 7.0

            onChange {
              angle = it
              markAsDirty()
            }
          }

          configurableDouble("x", x) {
            min = 0.0
            max = 1.0

            onChange {
              x = it
              markAsDirty()
            }
          }

          configurableDouble("y", y) {
            min = 0.0
            max = 1.0

            onChange {
              y = it
              markAsDirty()
            }
          }

          configurableEnum("Direction", layer.configuration.direction, Direction.entries) {
            onChange {
              layer.configuration.direction = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
