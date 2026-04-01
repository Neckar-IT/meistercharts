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
 * This enumeration represents a selection of axis.
 * Either both or just the x or y axis can be selected.
 */
enum class AxisSelection(
  /**
   * If the selection contains the x axis, this property returns true
   */
  val containsX: Boolean,
  /**
   * If the selection contains the y axis, this property returns true
   */
  val containsY: Boolean
) {
  /**
   * Zoom over both axis
   */
  Both(true, true),

  /**
   * Only zoom the x axis
   *
   * @noinspection FieldNamingConvention
   */
  X(true, false),

  /**
   * Only zoom the y axis
   *
   * @noinspection FieldNamingConvention
   */
  Y(false, true),

  /**
   * Do not zoom any axis
   */
  None(false, false);

  /**
   * Returns true if the given axis is contained in the selection
   */
  fun contains(axis: Axis): Boolean {
    return when (axis) {
      Axis.X -> containsX
      Axis.Y -> containsY
    }
  }

  /**
   * Returns the negated selection
   */
  fun negate(): AxisSelection {
    return when (this) {
      Both -> None
      X    -> Y
      Y    -> X
      None -> Both
    }
  }

  companion object {
    /**
     * Returns the axis selection
     */
    fun get(xSelected: Boolean, ySelected: Boolean): AxisSelection {
      return values().firstOrNull {
        it.containsX == xSelected && it.containsY == ySelected
      } ?: throw IllegalStateException("No Axis selection found for <$xSelected>, <$ySelected>")
    }
  }
}
