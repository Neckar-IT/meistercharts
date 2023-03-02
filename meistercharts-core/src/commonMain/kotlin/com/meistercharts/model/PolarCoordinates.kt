package com.meistercharts.model

import it.neckar.open.unit.si.rad
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents polar coordinates
 */
data class PolarCoordinates(
  val r: Double,
  val theta: @rad Double
) {

  /**
   *  Computes [Coordinates] from [PolarCoordinates]
   *  @see <a href="https://en.wikipedia.org/wiki/Polar_coordinate_system#Converting_between_polar_and_Cartesian_coordinates">Wikipedia</a>
   */
  fun toCartesian(): Coordinates {
    return Coordinates(toCartesianX(r, theta), toCartesianY(r, theta))
  }

  companion object {
    fun toCartesianY(r: Double, theta: @rad Double): Double = r * sin(theta)

    fun toCartesianX(r: Double, theta: @rad Double): Double = r * cos(theta)

    /**
     * Returns true if the current angle represents an angle that points to the right side
     */
    fun isToTheRight(theta: @rad Double): Boolean {
      val netTheta = theta % (2 * PI)
      return netTheta > PI * 1.5
        || netTheta < PI * 0.5 && netTheta > -PI * 0.5
        || netTheta < -PI * 1.5
    }

    fun isToTheTop(theta: @rad Double): Boolean {
      val netTheta = theta % (2 * PI)
      return netTheta > PI
        || (netTheta < 0.0 && netTheta > -PI)
    }
  }
}

