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

import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsets
import com.meistercharts.model.Insets
import it.neckar.open.observable.ObservableObject

/**
 */
class FittingWithMarginDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Content Area: Fitting with margin"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val insets = ObservableObject(Insets.of(30.0))

        contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()
        zoomAndTranslationDefaults {
          FittingWithMargin { insets.value }
        }

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
        }

        /**
         * It is necessary to resize the window to force updates
         */
        configurableInsets("Insets (Resize window for update)", insets.value) {
          onChange {
            insets.value = it
          }
        }
      }
    }
  }
}
