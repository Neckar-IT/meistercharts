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
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.ZeroLinesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Insets
import it.neckar.geometry.AxisSelection

/**
 *
 */
class ZeroLinesLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ec784909-c5f4-4dd3-9162-0ed00f681d29")
  override val name: String = "Zero Lines Layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.Companion.of(20.0))
        }

        configure {
          layers.addClearBackground()

          val zeroLinesLayer = ZeroLinesLayer()
          layers.addLayer(zeroLinesLayer)

          configurable("Axis to paint", zeroLinesLayer.configuration::axisToPaint, kotlin.enums.enumEntries<AxisSelection>().toList())
          configurable("Line width", zeroLinesLayer.configuration::lineWidth) {
            max = 10.0
          }
          configurableColorProvider("Color", zeroLinesLayer.configuration::color)
        }
      }
    }
  }
}
