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
package com.meistercharts.calc

import kotlin.math.pow

/**
 * Helper class that calculates the zoom level and factors
 *
 */
class ZoomLevelCalculator(
  val minZoomLevel: Int,
  val defaultZoomLevel: Int,
  val maxZoomLevel: Int
) {

  val minZoomFactor: Double
    get() = zoomLevel2Factor(minZoomLevel)

  val defaultZoomFactor: Double
    get() = zoomLevel2Factor(defaultZoomLevel)

  val maxZoomFactor: Double
    get() = zoomLevel2Factor(maxZoomLevel)

  companion object {
    const val SQRT_2: Double = 1.4142135623730951
    const val SQRT_2_TWICE: Double = 2.0

    /**
     * Converts a zoom level to a zoom factor
     */
    fun zoomLevel2Factor(zoomLevel: Int): Double {
      return SQRT_2.pow(zoomLevel.toDouble())
    }
  }
}
