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
 * A side of a four-sided shape.
 */
enum class Side {
  /**
   * The left side
   */
  Left,

  /**
   * The right side
   */
  Right,

  /**
   * The top side
   */
  Top,

  /**
   * The bottom side
   */
  Bottom;

  /**
   * Returns the flipped side, e. g. left becomes right and top becomes bottom
   */
  fun flipped(): Side {
    return when (this) {
      Left -> Right
      Right -> Left
      Top -> Bottom
      Bottom -> Top
    }
  }

  /**
   * Returns true if any of the provided sides matches this
   */
  fun any(side0: Side? = null, side1: Side? = null, side2: Side? = null, side3: Side? = null): Boolean {
    return this == side0 || this == side1 || this == side2 || this == side3
  }

  fun isLeftOrRight(): Boolean {
    return this == Left || this == Right
  }

  fun isTopOrBottom(): Boolean {
    return this == Top || this == Bottom
  }

  /**
   * Converts the side to a direction
   */
  fun toDirection(): Direction {
    return when (this) {
      Left -> Direction.CenterLeft
      Right -> Direction.CenterRight
      Top -> Direction.TopCenter
      Bottom -> Direction.BottomCenter
    }
  }
}
