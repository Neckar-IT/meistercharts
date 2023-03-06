/**
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

import com.meistercharts.algorithms.KeepCenterOnWindowResize
import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.impl.debugEnabled
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Insets

class ZoomAndTranslationDefaultsMarginDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Zoom + Translation Defaults: Margin"
  override val description: String = "##Visualizes the overscan\n Reset the zoom and translation with double click."
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        @Zoomed val margin = Insets(10.0, 50.0, 100.0, 150.0)
        val fittingWithMargin = FittingWithMargin(margin)

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addTextUnresolved("Margin: (10,50,100,150)", Color.gray)
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
          section("Resize") {
            comboBox("Behavior", layerSupport.chartSupport.windowResizeBehaviorProperty, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
              it::class.simpleName ?: it.toString()
            }
          }

          val zoomAndPanSupport = layerSupport.chartSupport.zoomAndTranslationSupport
          section("Reset") {
            button("Zoom") {
              zoomAndPanSupport.resetZoom(fittingWithMargin)
            }
            button("Translation") {
              zoomAndPanSupport.resetWindowTranslation(fittingWithMargin)
            }
            button("To Defaults") {
              zoomAndPanSupport.resetToDefaults(fittingWithMargin)
            }
          }
        }
      }
    }
  }
}
