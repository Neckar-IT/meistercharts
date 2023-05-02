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
package com.meistercharts.algorithms.layout

import com.meistercharts.model.Orientation

/**
 * The direction for the layout
 *
 */
enum class LayoutDirection(
  /**
   * Is set to true if the layout direction is from small screen values to larger screen value
   */
  val toScreenPositive: Boolean
) {

  /**
   * Distribute content from the center outwards (left and right)
   */
  CenterHorizontal(false),

  /**
   * Distribute content from the center up/downwards
   */
  CenterVertical(false),

  /**
   * Distribute content from left to right (typically for a horizontal layout)
   */
  LeftToRight(true),

  /**
   * Distribute content from right to left (typically for a horizontal layout)
   */
  RightToLeft(false),

  /**
   * Distribute content from top to bottom (typically for a vertical layout)
   */
  TopToBottom(true),

  /**
   * Distribute content from bottom to top (typically for a vertical layout)
   */
  BottomToTop(false), ;

  /**
   * Returns the orientation of the layout direction
   */
  val orientation: Orientation
    get() {
      return when (this) {
        LeftToRight, RightToLeft, CenterHorizontal -> Orientation.Horizontal
        CenterVertical, TopToBottom, BottomToTop -> Orientation.Vertical
      }

    }

  /**
   * Returns true if the layout direction is [CenterHorizontal]
   */
  val center: Boolean
    get() = this == CenterHorizontal
}

