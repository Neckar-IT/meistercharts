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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import it.neckar.open.kotlin.lang.random

/**
 * Simulates a slow paint operation by executing a configurable amount of CPU work.
 *
 * The work is a fixed number of arithmetic iterations - NOT a busy-wait on the clock:
 * the demos install a [it.neckar.open.time.VirtualNowProvider] whose time only advances
 * between frames, so polling `nowMillis()` inside paint never terminates. Deterministic
 * CPU work also makes the resulting frame times reflect the actual paint performance of
 * the platform (JS vs Wasm vs JVM) instead of just burning wall-clock time.
 *
 * The duration of a single work unit is platform-dependent (a few nanoseconds) -
 * use the PaintPerformanceLayer to see the resulting paint times.
 */
class SlowLayer(
  /**
   * The number of work units that are executed in each paint call
   */
  var workUnits: Int,

  /**
   * Random variation (+/-) that is applied to [workUnits] on every paint call
   */
  var workUnitsJitter: Int = 0,
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  /**
   * Accumulates the results of the work loop - prevents the compiler from eliminating the work as dead code
   */
  var checksum: Double = 0.0
    private set

  override fun paint(paintingContext: LayerPaintingContext) {
    val units = (workUnits + (random.nextDouble() * 2.0 - 1.0) * workUnitsJitter).toInt().coerceAtLeast(0)

    //Plain double arithmetic - compiles to native instructions on every target.
    //Transcendental functions (sin/cos) must be avoided here: on Wasm they are imported
    //from the JS host, which would measure interop-call overhead instead of CPU work.
    var accumulated = 1.0
    for (i in 0 until units) {
      accumulated = accumulated * 1.0000001 + 0.3
      if (accumulated > 1000.0) {
        accumulated *= 0.001
      }
    }
    checksum += accumulated
  }
}
