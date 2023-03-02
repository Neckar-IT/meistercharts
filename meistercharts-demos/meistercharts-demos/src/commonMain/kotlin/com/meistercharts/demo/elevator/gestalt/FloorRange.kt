package com.meistercharts.demo.elevator.gestalt

import com.meistercharts.annotations.DomainRelative

/**
 * Converts a story to a domain relative value
 */
class FloorRange(val numberOfFloors: @Floor Int = 5) {

  /**
   * The value per floor
   */
  val perFloor: @DomainRelative Double = 1.0 / numberOfFloors

  /**
   * Returns the center of the given floor in domain relative
   */
  fun floorCenter2DomainRelative(floorIndex: @Floor Double): @DomainRelative Double {
    return perFloor * (floorIndex + 0.5)
  }

  /**
   * Returns the bottom of the given floor in domain relative
   */
  fun floorBottom2DomainRelative(floorIndex: @Floor Double): @DomainRelative Double {
    return perFloor * floorIndex
  }
}
