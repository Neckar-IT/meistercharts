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

import com.meistercharts.algorithms.layers.crosswire.movable
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.configurableDouble

/**
 * How a movable cross wire could be set up
 */
class CrossWireLayerMovableDemoDescriptor : CrossWireLayerDemoDescriptor() {
  override val name: String = "Cross wire layer - movable"

  //language=HTML
  override val description: String = "## Cross wire layer that can be moved"
  override val category: DemoCategory = DemoCategory.Layers

  override fun configureDemo(chartingDemo: ChartingDemo) {
    super.configureDemo(chartingDemo)

    with(chartingDemo) {
      meistercharts {
        configure {
          val movable = crossWireLayer.movable(chartSupport.mouseCursorSupport, chartSupport.mouseEvents)
          layers.addLayer(movable)

          configurableDouble("padding bottom", movable.style::paddingBottom) {
            min = 0.0
            max = 50.0
          }
        }
      }
    }
  }

}
