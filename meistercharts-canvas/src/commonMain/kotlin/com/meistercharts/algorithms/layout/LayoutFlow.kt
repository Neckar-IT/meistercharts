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
 * Describes the direction in which elements are laid out
 */
@Deprecated("replace with LayoutDirection")
enum class LayoutFlow {
  /**
   * First to the right then to the center
   */
  ToRightCenter,

  /**
   * First to the right then to the top
   */
  ToRightToTop,

  /**
   * First to the right then to the bottom
   */
  ToRightToBottom,

  /**
   * First to the left then to the center
   */
  ToLeftCenter,

  /**
   * First to the left then to the top
   */
  ToLeftToTop,

  /**
   * First to the left then to the bottom
   */
  ToLeftToBottom,

  /**
   * First to the bottom then to the center
   */
  ToBottomCenter,

  /**
   * First to the bottom then to the left
   */
  ToBottomToLeft,

  /**
   * First to the bottom then to the right
   */
  ToBottomToRight,

  /**
   * First to the top then to the center
   */
  ToTopCenter,

  /**
   * First to the top then to the left
   */
  ToTopToLeft,

  /**
   * First to the top then to the right
   */
  ToTopToRight,

  ;

  /**
   * Returns the main flow orientation
   */
  val mainLayoutOrientation: Orientation
    get() {
      return when (this) {
        ToRightCenter -> Orientation.Horizontal
        ToRightToTop -> Orientation.Horizontal
        ToRightToBottom -> Orientation.Horizontal
        ToLeftCenter -> Orientation.Horizontal
        ToLeftToTop -> Orientation.Horizontal
        ToLeftToBottom -> Orientation.Horizontal
        ToBottomCenter -> Orientation.Vertical
        ToBottomToLeft -> Orientation.Vertical
        ToBottomToRight -> Orientation.Vertical
        ToTopCenter -> Orientation.Vertical
        ToTopToLeft -> Orientation.Vertical
        ToTopToRight -> Orientation.Vertical
      }
    }
}
