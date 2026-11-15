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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
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
import kotlin.math.sin
import kotlin.uuid.Uuid

/**
 * Compares the paint time of three math workloads per frame - visible in the performance layer:
 * - kotlin.math.sin: imported from the JS host on Wasm (one interop call per invocation)
 * - fastSin: pure-Kotlin polynomial - plain Wasm instructions, no interop
 * - plain arithmetic: baseline without any function call
 */
class SinPerformanceDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5cc368e3-18c9-4d33-bbff-3da8ca1aec37")
  override val name: String = "Sin Performance"
  override val description: String = "Compares kotlin.math.sin (JS import on Wasm), a pure-Kotlin fastSin polynomial and plain arithmetic - per-layer paint times are shown by the performance layer"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val stdlibSinLayer = MathWorkLayer("sin - kotlin.math (JS import on Wasm)") { iterations ->
            var accumulated = 0.0
            for (i in 0 until iterations) {
              accumulated += sin(i * 0.001)
            }
            accumulated
          }
          val fastSinLayer = MathWorkLayer("fastSin - pure Kotlin polynomial") { iterations ->
            var accumulated = 0.0
            for (i in 0 until iterations) {
              accumulated += fastSin(i * 0.001)
            }
            accumulated
          }
          val arithmeticLayer = MathWorkLayer("plain arithmetic - baseline") { iterations ->
            var accumulated = 1.0
            for (i in 0 until iterations) {
              accumulated = accumulated * 1.0000001 + 0.3
              if (accumulated > 1000.0) {
                accumulated *= 0.001
              }
            }
            accumulated
          }

          layers.addLayer(stdlibSinLayer)
          layers.addLayer(fastSinLayer)
          layers.addLayer(arithmeticLayer)
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

          configurableInt("Iterations per frame") {
            min = 0
            max = 20_000_000
            value = stdlibSinLayer.iterations

            onChange {
              stdlibSinLayer.iterations = it
              fastSinLayer.iterations = it
              arithmeticLayer.iterations = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

/**
 * Executes the given work on every paint call - the work duration shows up in the performance layer.
 */
private class MathWorkLayer(
  override val description: String,
  private val work: (iterations: Int) -> Double,
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  var iterations: Int = 1_000_000

  /**
   * Accumulates the work results - prevents the compiler from eliminating the work as dead code
   */
  var checksum: Double = 0.0
    private set

  override fun paint(paintingContext: LayerPaintingContext) {
    checksum += work(iterations)
  }
}
