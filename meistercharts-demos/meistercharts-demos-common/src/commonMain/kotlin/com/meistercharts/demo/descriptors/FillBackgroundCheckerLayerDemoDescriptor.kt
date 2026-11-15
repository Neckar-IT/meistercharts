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
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble

/**
 * Very simple demo that shows how to clear the background
 */
class FillBackgroundCheckerLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("2b3d0e33-06a1-4756-9126-26e77704898f")
  override val name: String = "Background checker layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val layer = layers.addBackgroundChecker()

          configurable("segment width", layer.configuration::segmentWith) {
            max = 100.0
          }
          configurable("segment height", layer.configuration::segmentHeight) {
            max = 100.0
          }

          configurableColorProvider("Color 0", layer.configuration::background0)
          configurableColorProvider("Color 1", layer.configuration::background1)
        }
      }
    }
  }
}

