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

import it.neckar.open.kotlin.lang.toIntCeil
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.unit.si.rad
import kotlinx.serialization.Serializable
import kotlin.math.PI

@Serializable
data class RotatedRectangle(
  val center: Coordinates,
  val size: Size,
  val rotation: @rad Double,
) {
  /**
   * Returns a new [RotatedRectangle] with the given center rotated by the given angle
   */
  fun rotated(angle: @rad Double): RotatedRectangle {
    return createNormalized(center, size, rotation + angle)
  }

  /**
   * Returns a list of vertices (coordinates of corners) in the order: topLeft, topRight, bottomRight, bottomLeft
   */
  fun vertices(): List<Coordinates> {
    val halfWidth = size.width / 2
    val halfHeight = size.height / 2
    val topLeft = Coordinates(-halfWidth, halfHeight)
    val topRight = Coordinates(halfWidth, halfHeight)
    val bottomRight = Coordinates(halfWidth, -halfHeight)
    val bottomLeft = Coordinates(-halfWidth, -halfHeight)
    return listOf(topLeft, topRight, bottomRight, bottomLeft).map {
      it.toPolar().rotated(rotation).toCartesian() + center
    }.inCssOrder()
  }

  fun toRectangle(): Rectangle {
    check(rotation == 0.0) { "Cannot convert a rotated rectangle to a Rectangle" }
    return Rectangle.fromOrigin(center, size, Direction.Center)
  }

  fun toQuadrilateral(): Quadrilateral {
    return Quadrilateral.fromList(vertices())
  }


  companion object {
    /**
     * Creates a new [RotatedRectangle] with the given center, size, and rotation.
     * The rotation is normalized to be in the range of -PI/4 to PI/4 (-45 to 45 degrees).
     */
    fun createNormalized(center: Coordinates, size: Size, rotation: @rad Double): RotatedRectangle {
      val quarterRotations = rotation / (PI / 2)
      val remainder = quarterRotations % 1

      /**
       * If the remainder is greater than 0.5, we round up, otherwise we round down.
       * This is to ensure that the rotation is always in the range of -PI/4 to PI/4 (-45 to 45 degrees).
       * If the rotation is outside of this range, we adjust the size and rotation accordingly.
       * This is done by quantizing the rotation to the nearest multiple of PI/2.
       */
      val quantizedQuarterRotations = if (remainder > 0.5) {
        if (quarterRotations > 0) quarterRotations.toIntCeil() else quarterRotations.toIntFloor()
      } else {
        if (quarterRotations > 0) quarterRotations.toIntFloor() else quarterRotations.toIntCeil()
      }
      val normalizedRotation = rotation - quantizedQuarterRotations * (PI / 2)
      // If the [RotatedRectangle] is rotated by a multiple of 90 degrees, we need to adjust the size
      val adjustedSize = if (quantizedQuarterRotations % 2 == 0) size else Size(size.height, size.width)
      return RotatedRectangle(center, adjustedSize, normalizedRotation)
    }
  }
}
