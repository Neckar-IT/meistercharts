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
