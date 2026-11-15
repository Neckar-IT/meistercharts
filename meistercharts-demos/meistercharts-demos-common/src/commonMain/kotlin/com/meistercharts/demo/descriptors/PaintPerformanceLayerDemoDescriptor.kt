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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.SlowLayer
import com.meistercharts.algorithms.layers.debug.addPerformanceLayer
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableInt
import kotlin.uuid.Uuid

/**
 * Visualizes the paint performance (PaintPerformanceLayer) under a configurable, simulated CPU load per frame.
 */
class PaintPerformanceLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("0a0fa424-175d-4d0d-a169-4e65a508c1e6")
  override val name: String = "Paint Performance"
  override val description: String = "Visualizes the paint times while a configurable amount of CPU work is executed on every frame"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val slowLayer = SlowLayer(workUnits = 10_000_000, workUnitsJitter = 2_000_000)
          layers.addLayer(slowLayer)
          layers.addPerformanceLayer()

          //The performance layer visualizes the repaint statistics - they have to be recorded
          chartSupport.layerSupport.recordPaintStatistics = true

          //Repaint continuously so the performance layer shows a live measurement
          var continuousRepaint = true
          chartSupport.onRenderPrepend { _, _, _ ->
            if (continuousRepaint) {
              chartSupport.layerSupport.markAsDirty(DirtyReason.Animation)
            }
          }

          configurableBoolean("Continuous repaint") {
            value = continuousRepaint
            onChange {
              continuousRepaint = it
              markAsDirty()
            }
          }

          configurableInt("Work units per frame") {
            min = 0
            max = 100_000_000
            value = slowLayer.workUnits

            onChange {
              slowLayer.workUnits = it
              markAsDirty()
            }
          }

          configurableInt("Work units jitter (+/-)") {
            min = 0
            max = 20_000_000
            value = slowLayer.workUnitsJitter

            onChange {
              slowLayer.workUnitsJitter = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
