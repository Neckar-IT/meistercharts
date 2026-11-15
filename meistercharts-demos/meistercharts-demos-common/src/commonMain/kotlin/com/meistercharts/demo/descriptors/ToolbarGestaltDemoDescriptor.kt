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
import com.meistercharts.zoom.FittingInContentViewport
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.charts.ToolbarGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.model.Insets

/**
 *
 */
class ToolbarGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e17a5625-e83a-4916-b3d6-de7f2632357e")
  override val name: String = "Toolbar Gestalt"
  override val description: String = "Sample for a toolbar"

  override val quality: DemoQuality = DemoQuality.High

  override val variabilityType: VariabilityType = VariabilityType.Stable


  override val category: DemoCategory = DemoCategory.Gestalt

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      val toolbarGestalt = ToolbarGestalt()

      meistercharts {
        toolbarGestalt.configure(this)

        zoomAndTranslationDefaults {
          FittingInContentViewport
        }

        configure {
          layers.addClearBackground()

          chartSupport.rootChartState.contentViewportMargin = Insets.of(50.0)
          layers.addLayer(ContentAreaDebugLayer())
        }
      }
    }
  }
}
