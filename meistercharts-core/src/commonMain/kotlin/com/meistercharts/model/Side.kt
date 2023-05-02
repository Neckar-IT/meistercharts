/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.model

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
