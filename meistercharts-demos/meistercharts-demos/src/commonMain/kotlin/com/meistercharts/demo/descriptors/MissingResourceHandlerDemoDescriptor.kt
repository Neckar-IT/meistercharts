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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.algorithms.layers.addShowLoadingOnMissingResources
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean

/**
 *
 */
class MissingResourceHandlerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Missing Resources Handler"

  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addBackgroundChecker()

          val missingResourceLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var resourceMissing = true

            override fun paint(paintingContext: LayerPaintingContext) {
              if (resourceMissing) {
                paintingContext.missingResources.reportMissing("http://blafasel.com")
              }
            }
          }
          layers.addLayer(missingResourceLayer)

          layers.addShowLoadingOnMissingResources()

          configurableBoolean("Resource missing", missingResourceLayer::resourceMissing)
        }
      }
    }
  }
}
