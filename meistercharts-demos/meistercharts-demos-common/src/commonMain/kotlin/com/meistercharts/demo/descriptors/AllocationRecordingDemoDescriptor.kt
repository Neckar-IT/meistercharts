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
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.canvas.allocation.AllocationRecordingEngine
import com.meistercharts.canvas.allocation.AllocationRecordingMode
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import kotlin.uuid.Uuid

/**
 * Demo to verify the allocation recording: [AllocatingInPaintLayer] deliberately allocates known
 * objects on every paint, and the demo turns the recording on. Open it, and the
 * [com.meistercharts.algorithms.layers.debug.AllocationRecordingLayer] overlay (top-right) should show
 * the deliberately allocated types - most prominently `AllocationDemoMarker`, plus `double[]` and
 * `ArrayList`.
 *
 * Toggle the mode with Ctrl+Shift+Alt+A. ATTENTION: the overlay only shows data on the JVM (JavaFX);
 * in the browser (JS, Wasm) the recording is inert.
 */
class AllocationRecordingDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b7e4c1a2-9f3d-4e8a-bc21-5d6f0a1e2b3c")
  override val name: String = "Allocation Recording"
  override val description: String = "Deliberately allocates in paint to verify the allocation recording overlay (JVM only)"
  //A developer/diagnostic demo: the Layers category is DevOnly, so it is hidden in the curated default
  //view and revealed via the "Show all" toggle in the JavaFX demo app.
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.Low
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolved("Allocation Recording - the overlay (top-right) shows what is allocated in paint. Ctrl+Shift+Alt+A toggles the mode. JVM only.")
          layers.addLayer(AllocatingInPaintLayer())

          //Turn the recording on so the overlay shows data immediately; restore when leaving the demo.
          AllocationRecordingEngine.mode = AllocationRecordingMode.ByTypeAndStacktrace
          chartSupport.onDispose {
            AllocationRecordingEngine.mode = AllocationRecordingMode.Off
          }
        }
      }
    }
  }
}

/**
 * A layer that deliberately allocates a known set of objects on every paint - the input the allocation
 * recording is supposed to detect.
 */
private class AllocatingInPaintLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  /**
   * Keeps the allocations reachable so escape analysis cannot optimize them away.
   */
  private var sink: Any? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    repeat(300) { index ->
      sink = AllocationDemoMarker(index)
    }
    repeat(30) {
      sink = DoubleArray(8)
    }
    repeat(5) {
      sink = ArrayList<Int>(16)
    }
  }
}

/**
 * A distinctive marker type so it is easy to recognize in the allocation report overlay.
 */
private class AllocationDemoMarker(val index: Int)
