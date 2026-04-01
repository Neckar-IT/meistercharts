/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.geometry

import it.neckar.open.unit.si.rad
import kotlin.js.JsExport
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents polar coordinates
 */
@JsExport
data class PolarCoordinates(
  val r: Double,
  val theta: @rad Double,
) {

  /**
   *  Computes [Coordinates] from [PolarCoordinates]
   *  @see <a href="https://en.wikipedia.org/wiki/Polar_coordinate_system#Converting_between_po lar_and_Cartesian_coordinates">Wikipedia</a>
   */
  fun toCartesian(): Coordinates {
    return Coordinates(toCartesianX(r, theta), toCartesianY(r, theta))
  }

  /**
   * Rotates the angle by the given angle
   */
  fun rotated(angle: @rad Double): PolarCoordinates {
    return PolarCoordinates(r, theta + angle)
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

