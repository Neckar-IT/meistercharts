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
package com.meistercharts.demo.descriptors.zeitstrahl

import kotlin.math.min

/**
 * Layout of the demo: the tree column on the left; the path, the axis on top, the period strip at the bottom and the lanes in between.
 */
object ZeitstrahlPocLayout {
  /** The column `STRUKTUR`; it stays in place while the time zooms and pans. */
  const val TreeWidth: Double = 260.0
  const val PathHeight: Double = 40.0
  const val AxisHeight: Double = 40.0
  const val LanesTop: Double = PathHeight + AxisHeight
  const val StripBreadcrumbHeight: Double = 18.0
  const val StripRowHeight: Double = 20.0

  /** A section leaves 2 px of its row free below it. */
  const val StripSectionHeight: Double = StripRowHeight - 2.0
  const val StripRowCount: Int = 3
  const val StripHeight: Double = StripBreadcrumbHeight + StripRowHeight * StripRowCount
  const val LaneTitleHeight: Double = 30.0

  /** The space below the last row of a lane. */
  const val LaneBottomPadding: Double = 6.0

  /** The heights of the lanes in [available] px: every lane gets what it asks for, or an equal share of what the smaller lanes leave. */
  fun laneHeights(desired: List<Double>, available: Double): List<Double> {
    val heights = MutableList(desired.size) { 0.0 }
    var remaining = available
    desired.indices.sortedBy { desired[it] }.forEachIndexed { done, index ->
      heights[index] = min(desired[index], remaining / (desired.size - done))
      remaining -= heights[index]
    }
    return heights
  }
}
