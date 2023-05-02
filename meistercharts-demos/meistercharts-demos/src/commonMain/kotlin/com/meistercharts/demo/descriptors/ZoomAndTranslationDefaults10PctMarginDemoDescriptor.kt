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
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.impl.debugEnabled
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

class ZoomAndTranslationDefaults10PctMarginDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Zoom + Translation Defaults: 10% Margin"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addTextUnresolved("Margin: 10%", Color.gray)
        }

        zoomAndTranslationDefaults { ZoomAndTranslationDefaults.tenPercentMargin }

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
        }
      }
    }
  }
}
