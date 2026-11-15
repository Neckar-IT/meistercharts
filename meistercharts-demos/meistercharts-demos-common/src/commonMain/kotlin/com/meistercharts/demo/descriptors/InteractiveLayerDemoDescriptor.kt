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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.layer.InteractiveKeyDemoLayer
import com.meistercharts.demo.InteractiveMouseClicksDemoLayer
import com.meistercharts.demo.InteractiveMouseDragsDemoLayer
import com.meistercharts.demo.VariabilityType

class InteractiveLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("a36f4c48-55bb-4bf1-80ee-217fbf3ad8d7")
  override val name: String = "Interactions"
  override val description: String = "Handling of mouse-click and key events"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          /**
           * Mouse clicks result in gray dots
           */
          layers.addLayer(InteractiveMouseClicksDemoLayer())
          layers.addLayer(InteractiveMouseDragsDemoLayer())
          layers.addLayer(InteractiveKeyDemoLayer())
        }
      }
    }
  }
}
