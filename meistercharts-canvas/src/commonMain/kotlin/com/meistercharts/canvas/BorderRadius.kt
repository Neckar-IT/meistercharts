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
package com.meistercharts.canvas

import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.kotlin.lang.isPositiveOrZero

/**
 * Contains the radii for four corners
 */
data class BorderRadius(
  @PositiveOrZero val topLeft: Double,
  @PositiveOrZero val topRight: Double,
  @PositiveOrZero val bottomRight: Double,
  @PositiveOrZero val bottomLeft: Double
) {

  init {
    require(topLeft.isPositiveOrZero()) { "topLeft must be >= 0 but was <$topLeft>" }
    require(topRight.isPositiveOrZero()) { "topRight must be >= 0 but was <$topRight>" }
    require(bottomRight.isPositiveOrZero()) { "bottomRight must be >= 0 but was <$bottomRight>" }
    require(bottomLeft.isPositiveOrZero()) { "bottomLeft must be >= 0 but was <$bottomLeft>" }
  }

  /**
   * Returns true if all four values are 0.0
   */
  val isEmpty: Boolean
    get() {
      return topLeft == 0.0 &&
        topRight == 0.0 &&
        bottomRight == 0.0 &&
        bottomLeft == 0.0
    }

  companion object {
    /**
     * All four corner radii are set to 0
     */
    val none: BorderRadius = BorderRadius(0.0, 0.0, 0.0, 0.0)

    val all2: BorderRadius = of(2.0)
    val all5: BorderRadius = of(5.0)

    /**
     * All four corner radii are set to [radius]
     */
    fun of(radius: @PositiveOrZero Double): BorderRadius = BorderRadius(radius, radius, radius, radius)
  }
}
