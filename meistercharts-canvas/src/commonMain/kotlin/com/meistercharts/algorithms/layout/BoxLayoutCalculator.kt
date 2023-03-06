/**
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
package com.meistercharts.algorithms.layout

import com.meistercharts.annotations.Zoomed
import it.neckar.open.kotlin.lang.ceil
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.unit.other.px

/**
 * Calculates a box layout
 *
 */
object BoxLayoutCalculator {
  /**
   * Calculates a box layout with (equisized boxes)
   */
  fun layout(
    /**
     * The available space (for all boxes)
     */
    availableSpace: @px Double,

    /**
     * The number of boxes that will be placed in the available space (if possible)
     */
    numberOfBoxes: Int,

    /**
     * The layout direction
     */
    layoutDirection: LayoutDirection,

    /**
     * The minimal size of a box
     */
    minBoxSize: @px Double = 0.0,

    /**
     * The maximum size of a box
     */
    maxBoxSize: @px Double? = null,

    /**
     * The gap between two boxes
     */
    gapSize: Double = 0.0,

    /**
     * The layout mode
     */
    layoutMode: LayoutMode = Exact,
  ): EquisizedBoxLayout {
    val boxSize = calculateBoxSize(availableSpace, numberOfBoxes, minBoxSize, maxBoxSize, gapSize, layoutMode)
    return EquisizedBoxLayout(availableSpace, numberOfBoxes, boxSize, gapSize, layoutDirection)
  }
}

/**
 * Calculates the size for each box
 */
internal fun calculateBoxSize(
  /**
   * the amount of space that is available
   */
  availableSpace: @px Double,
  /**
   * the number of boxes that should be placed
   */
  numberOfBoxes: Int,
  /**
   * the minimum size of every box
   */
  minBoxSize: @px Double = 0.0,
  /**
   * the maximum size of every box
   */
  maxBoxSize: @px Double?,

  /**
   * The size of one gap
   */
  gapSize: Double,

  /**
   * The layout mode
   */
  layoutMode: LayoutMode,
): @Zoomed @px Double {
  // We must be able to handle the case with no boxes at all!
  if (numberOfBoxes < 1) {
    // no boxes -> just return the minimum box size
    return layoutMode.ceil(minBoxSize)
  }

  //The total size for *all* gaps
  val totalGapSize = (numberOfBoxes - 1) * layoutMode.floor(gapSize)

  //The size for *one* box
  val optimalSizeForOneBox = (availableSpace - totalGapSize) / numberOfBoxes
  val roundedSizeForOneBox = layoutMode.floor(optimalSizeForOneBox)

  return roundedSizeForOneBox
    .coerceIn(layoutMode.ceil(minBoxSize), layoutMode.floor(maxBoxSize))
}


sealed interface LayoutMode {
  /**
   * Returns the ceil - or exact value
   */
  fun ceil(value: @px Double): @px Double

  fun floor(value: @px Double?): @px Double?

  fun floor(value: @px Double): @px Double
}

/**
 * Exact values - no rounding whatsoever
 */
object Exact : LayoutMode {
  override fun ceil(value: Double): Double {
    return value
  }

  override fun floor(value: Double?): Double? {
    return value
  }

  override fun floor(value: Double): Double {
    return value
  }
}

/**
 * Rounded to the nearest integer
 */
object Rounded : LayoutMode {
  override fun ceil(value: Double): Double {
    return value.ceil()
  }

  override fun floor(value: Double?): Double? {
    return value?.floor()
  }

  override fun floor(value: Double): Double {
    return value.floor()
  }
}
