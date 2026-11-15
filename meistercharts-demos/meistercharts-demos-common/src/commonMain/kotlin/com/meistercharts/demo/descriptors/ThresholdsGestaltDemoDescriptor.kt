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
import com.meistercharts.charts.ThresholdsGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableList
import it.neckar.geometry.Orientation

/**
 * Demonstrates the self-contained [ThresholdsGestalt] (thresholds without a value axis).
 */
class ThresholdsGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b2f4d6e8-3a1c-4b5d-9e70-1c2d3e4f5a6b")
  override val name: String = "Thresholds Gestalt"
  override val description: String = "Thresholds visualized with the self-contained ThresholdsGestalt (no value axis)."
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val gestalt = ThresholdsGestalt()

        configure {
          layers.addClearBackground()
        }
        gestalt.configure(this)

        configurableList("Orientation", Orientation.Vertical, Orientation.entries) {
          onChange { orientation ->
            when (orientation) {
              Orientation.Vertical -> gestalt.configuration.applyVerticalConfiguration()
              Orientation.Horizontal -> gestalt.configuration.applyHorizontalConfiguration()
            }
            markAsDirty()
          }
        }
      }
    }
  }
}
