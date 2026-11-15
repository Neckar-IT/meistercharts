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
import com.meistercharts.zoom.BoundsProvider
import com.meistercharts.zoom.FittingInContentViewport
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.ContentViewportDebugLayer
import com.meistercharts.color.Color
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.section
import com.meistercharts.model.Insets

/**
 *
 */
class ContentAlwaysVisibleDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("a55f66b9-d6ab-4caf-b1e1-5880522cba3a")
  override val name: String = "Zoom/T. Modifier: Content Always visible"
  override val category: DemoCategory = DemoCategory.Interaction
  override val description: String = "Zoom and translate while the content area always stays completely visible"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(20.0, 30.0, 40.0, 50.0))
        contentViewportGestalt.configure(this)

        @ContentAreaRelative var top = 0.0
        @ContentAreaRelative var left = 0.0

        @ContentAreaRelative var right = 1.0
        @ContentAreaRelative var bottom = 1.0

        zoomAndTranslationModifier {
          contentAlwaysCompletelyVisible(boundsProvider = object : BoundsProvider {
            override fun left(): @ContentAreaRelative Double {
              return left
            }

            override fun right(): @ContentAreaRelative Double {
              return right
            }

            override fun top(): @ContentAreaRelative Double {
              return top
            }

            override fun bottom(): @ContentAreaRelative Double {
              return bottom
            }
          })
        }

        zoomAndTranslationDefaults {
          FittingInContentViewport
        }

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(ContentViewportDebugLayer())
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              gc.stroke(Color.orange)
              gc.strokeRectCoordinates(
                chartCalculator.contentAreaRelative2windowX(left),
                chartCalculator.contentAreaRelative2windowY(top),
                chartCalculator.contentAreaRelative2windowX(right),
                chartCalculator.contentAreaRelative2windowY(bottom)
              )
            }
          })
        }

        configurable("Content Viewport Margin", contentViewportGestalt::contentViewportMargin)

        section("Bounds @ContentAreaRelative")

        configurableDouble("Top", top) {
          min = -2.0
          max = 2.0

          onChange {
            top = it
            markAsDirty()
          }
        }
        configurableDouble("Left", left) {
          min = -2.0
          max = 2.0

          onChange {
            left = it
            markAsDirty()
          }
        }
        configurableDouble("Bottom", bottom) {
          min = -2.0
          max = 2.0

          onChange {
            bottom = it
            markAsDirty()
          }
        }
        configurableDouble("Right", right) {
          min = -2.0
          max = 2.0

          onChange {
            right = it
            markAsDirty()
          }
        }
      }
    }
  }
}
