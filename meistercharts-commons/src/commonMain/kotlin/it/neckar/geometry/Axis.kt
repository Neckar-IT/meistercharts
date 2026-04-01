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

/**
 * Identifies the axis
 */
enum class Axis {
  /**
   * The X axis (top --> down)
   */
  X,

  /**
   * The Y axis (left --> right)
   */
  Y;
}

/**
 * Extracts the correct value for the size depending on the given axis
 */
fun Axis.extract(size: Size): Double {
  return when (this) {
    Axis.X -> size.width
    Axis.Y -> size.height
  }
}

/**
 * Extracts the correct value for the coordinates depending on the given axis
 */
fun Axis.extract(coordinates: Coordinates): Double {
  return when (this) {
    Axis.X -> coordinates.x
    Axis.Y -> coordinates.y
  }
}

/**
 * Extracts the correct value for the distance depending on the given axis
 */
fun Axis.extract(distance: Distance): Double {
  return when (this) {
    Axis.X -> distance.x
    Axis.Y -> distance.y
  }
}
