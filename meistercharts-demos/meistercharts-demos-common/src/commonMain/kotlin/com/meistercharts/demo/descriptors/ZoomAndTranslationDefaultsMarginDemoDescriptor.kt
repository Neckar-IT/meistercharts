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
import com.meistercharts.algorithms.layers.text.addTextUnresolvedColored
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.model.Insets
import com.meistercharts.resize.KeepCenterOnWindowResize
import com.meistercharts.resize.KeepOriginOnWindowResize
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.debugEnabled

class ZoomAndTranslationDefaultsMarginDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("1a9c85fc-2f5d-430f-9a2c-9d017245d3cb")
  override val name: String = "Zoom + Translation Defaults: Margin"
  override val description: String = "Visualizes the overscan margin. Double click resets zoom and translation."
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        @Zoomed val margin = Insets(10.0, 50.0, 100.0, 150.0)
        val fittingWithMargin = FittingWithMargin(margin)

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addTextUnresolvedColored("Margin: (10,50,100,150)", Color.gray)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.stroke(Color.cyan)
              gc.strokeRect(margin.left, margin.top, gc.width - margin.offsetWidth, gc.height - margin.offsetHeight)
            }
          })

          //resizeBehavior = KeepPasspartout(padding)
        }

        zoomAndTranslationDefaults { fittingWithMargin }

        zoomAndTranslationModifier {
          minZoom(0.1, 0.1)
          maxZoom(5.0, 5.0)
          contentAlwaysBarelyVisible()

          //If enabled only positive movements are allowed
          //.withOnlyPositivePanModifier()
          //.withDisabledPanningX()
          //.withDisabledZoomingAndPanning()
          debugEnabled()
        }

        declare { layerSupport ->
          section("Resize")
          configurableListWithProperty("Behavior", layerSupport.chartSupport::windowResizeBehavior, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
            converter {
              it::class.simpleName ?: it.toString()
            }
          }

          val zoomAndPanSupport = layerSupport.chartSupport.zoomAndTranslationSupport
          section("Reset")
          button("Zoom") {
            zoomAndPanSupport.resetZoom(fittingWithMargin, reason = UpdateReason.UserInteraction)
          }
          button("Translation") {
            zoomAndPanSupport.resetWindowTranslation(fittingWithMargin, reason = UpdateReason.UserInteraction)
          }
          button("To Defaults") {
            zoomAndPanSupport.resetToDefaults(fittingWithMargin, reason = UpdateReason.UserInteraction)
          }
        }
      }
    }
  }
}
