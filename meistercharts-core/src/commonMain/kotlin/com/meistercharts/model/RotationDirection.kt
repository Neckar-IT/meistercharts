package com.meistercharts.model

import it.neckar.open.unit.si.rad

/**
 * Represents at the rotation direction.
 * To calculate an angle from a given direction to another use
 * [toClockwise] and [toCounterClockwise]
 */
enum class RotationDirection(
  /**
   * The factor that has to be applied when converting from clockwise to counter clockwise
   */
  private val toClockwise: Int,
  /**
   * The factor that has to be applied when converting from counter clockwise to clockwise
   */
  private val toCounterClockwise: Int
) {
  CounterClockwise(-1, 1),
  Clockwise(1, -1);

  /**
   * Factor for calculating from this enum value to Clockwise
   */
  fun toClockwise(@rad value: Double): @rad Double {
    return value * toClockwise
  }

  /**
   * Converts the value to the plattform rotation direction (clockwise)
   */
  fun toPlattformRotationDirection(@rad value: Double): @rad Double {
    return toClockwise(value)
  }

  /**
   * Factor for calculating from this enum value to CounterClockwise
   */
  fun toCounterClockwise(value: @rad Double): @rad Double {
    return value * toCounterClockwise
  }
}
