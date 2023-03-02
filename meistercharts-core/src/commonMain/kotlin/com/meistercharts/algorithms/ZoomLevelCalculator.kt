package com.meistercharts.algorithms

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

    /**
     * Converts a zoom level to a zoom factor
     */
    fun zoomLevel2Factor(zoomLevel: Int): Double {
      return SQRT_2.pow(zoomLevel.toDouble())
    }
  }
}
