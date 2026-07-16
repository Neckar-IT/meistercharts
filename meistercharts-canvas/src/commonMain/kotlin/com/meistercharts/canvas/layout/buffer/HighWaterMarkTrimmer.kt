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
package com.meistercharts.canvas.layout.buffer

import com.meistercharts.loop.PaintingLoopIndex
import it.neckar.open.annotations.Hot
import kotlin.math.ceil
import kotlin.math.max

/**
 * Hysteresis trim policy for grow-only buffers.
 *
 * Grow-only buffers keep their peak capacity forever. Trimming naively is wrong: never trimming
 * wastes memory, trimming every loop causes thrashing (shrink then immediately regrow). This policy
 * tracks the peak demand over a window of painting loops and only shrinks at epoch boundaries, with
 * [slackFactor] as headroom against regrow-thrashing. Result: no trim during normal fluctuation,
 * exactly one trim some loops after a real, sustained decline.
 *
 * The epoch clock is driven by the [PaintingLoopIndex] (the same trigger as
 * [MappedLayoutBuffer.resetIfNewLoopIndex]). The loop index value is only compared for equality,
 * never used for arithmetic, so its overflow is irrelevant.
 */
class HighWaterMarkTrimmer(
  /**
   * The window length in painting loops. A trim is considered once per window.
   */
  val windowSize: Int = DefaultWindowSize,
  /**
   * The headroom kept above the window peak demand. `0.5` keeps 50% more capacity than the peak
   * demand of the window, absorbing oscillating demand without regrow-thrashing.
   */
  val slackFactor: Double = DefaultSlackFactor,
) {
  init {
    require(windowSize > 0) { "windowSize must be > 0 but was $windowSize" }
    require(slackFactor >= 0.0) { "slackFactor must be >= 0 but was $slackFactor" }
  }

  /**
   * The loop index of the last processed loop. Guards against processing the same loop twice.
   */
  private var currentLoopIndex: PaintingLoopIndex = PaintingLoopIndex.Unknown

  /**
   * Number of loops processed since the current window started.
   */
  private var loopsSinceEpochStart: Int = 0

  /**
   * The peak demand observed within the current window.
   */
  private var windowPeakDemand: Int = 0

  /**
   * Records the demand of the current loop and, at epoch boundaries (every [windowSize] loops),
   * returns the target capacity to shrink to. Returns `null` when no trimming should happen this
   * loop - either because the window is not full yet or because [currentCapacity] is already within
   * the target.
   *
   * demand: the number of slots actually needed in the loop that is being processed
   * currentCapacity: the currently allocated capacity of the buffer
   */
  @Hot
  fun pollTrimTarget(paintingLoopIndex: PaintingLoopIndex, demand: Int, currentCapacity: Int): Int? {
    if (paintingLoopIndex == currentLoopIndex) {
      //Same loop - already processed. Do not advance the epoch or double-count the demand.
      return null
    }

    currentLoopIndex = paintingLoopIndex
    windowPeakDemand = max(windowPeakDemand, demand)
    loopsSinceEpochStart++

    if (loopsSinceEpochStart < windowSize) {
      return null
    }

    //Epoch boundary reached: decide whether to trim, then start a fresh window
    val targetCapacity: Int = ceil(windowPeakDemand * (1.0 + slackFactor)).toInt()
    loopsSinceEpochStart = 0
    windowPeakDemand = 0

    return if (currentCapacity > targetCapacity) targetCapacity else null
  }

  companion object {
    /**
     * Default window length in painting loops (~2s at 60fps).
     */
    const val DefaultWindowSize: Int = 128

    /**
     * Default headroom above the window peak demand.
     */
    const val DefaultSlackFactor: Double = 0.5
  }
}
