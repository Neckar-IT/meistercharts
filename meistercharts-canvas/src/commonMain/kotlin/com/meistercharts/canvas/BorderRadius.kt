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
