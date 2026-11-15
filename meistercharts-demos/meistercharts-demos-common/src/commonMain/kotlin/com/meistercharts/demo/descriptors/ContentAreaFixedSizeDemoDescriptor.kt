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

import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import com.meistercharts.resize.KeepCenterOnWindowResize
import com.meistercharts.resize.KeepLocation
import com.meistercharts.resize.KeepOriginOnWindowResize
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import it.neckar.geometry.Direction
import it.neckar.geometry.Size

/**
 * Shows a content area with a fixed size of 800x600
 */
class ContentAreaFixedSizeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("26651975-432d-451e-8ee1-f599c12aa0d7")
  override val name: String = "Content Area Resizing: Fixed Size"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Shows a content area with a fixed size of 800x600"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        contentAreaSizingStrategy = FixedContentAreaSize(Size(500.0, 500.0))

        configure {
          chartSupport.windowResizeBehavior = KeepOriginOnWindowResize

          layers.addClearBackground()
          layers.addLayer(WindowDebugLayer())
          layers.addLayer(ContentAreaDebugLayer())
          layers.addTextUnresolved("Fixed content area size: 800x600") {
            textColor = Color.blue
            anchorDirection = Direction.BottomLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
          }
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator
              val chartState = chartCalculator.chartState

              check(gc.width == chartState.windowSize.width) {
                "Different widths: ${gc.width} != ${chartState.windowSize.width}"
              }
              check(gc.height == chartState.windowSize.height) {
                "Different heights: ${gc.height} != ${chartState.windowSize.height}"
              }

              gc.lineWidth = 2.0
              gc.stroke(Color.orangered)
              @Window val centerX = gc.width / 2.0
              @Window val centerY = gc.height / 2.0
              gc.strokeOvalCenter(centerX, centerY, 40.0, 40.0)

              gc.fill(Color.chocolate)
              gc.fillText("Window: $centerX/$centerY", centerX + 24.0, centerY - 4.0, Direction.BottomLeft)
              gc.fillText("Content Area Relative: ${chartCalculator.window2contentAreaRelativeX(centerX)}/${chartCalculator.window2contentAreaRelativeY(centerY)}", centerX + 24.0, centerY + 4.0, Direction.TopLeft)
            }
          })

          section("Resize")

          configurableListWithProperty(
            propertyName = "Behavior", property = chartSupport::windowResizeBehavior,
            possibleValues = listOf(
              KeepOriginOnWindowResize,
              KeepCenterOnWindowResize,
              ResetToDefaultsOnWindowResize,
              KeepLocation(0.8, 0.5)
            )
          ) {
            converter {
              when (it) {
                is KeepLocation -> {
                  "${it.windowRelativeX}/${it.windowRelativeY}"
                }

                is KeepOriginOnWindowResize -> {
                  "Keep Origin"
                }

                is KeepCenterOnWindowResize -> {
                  "Keep Center"
                }

                is ResetToDefaultsOnWindowResize -> {
                  "Reset to defaults"
                }

                else -> it::class.simpleName ?: it.toString()
              }
            }
          }
        }
      }
    }
  }
}
