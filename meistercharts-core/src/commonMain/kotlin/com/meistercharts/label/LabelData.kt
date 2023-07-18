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
package com.meistercharts.label

import com.meistercharts.color.Color

/**
 * Describes the data for a label.
 *
 * Does *not* contain the location where it is painted. Just the details required to render the label itself
 */
@Deprecated("No longer used")
data class LabelData(
  /**
   * The text of the label
   */
  val text: String,

  /**
   * The color of the label
   */
  val color: Color

  //TODO add border color(?) or style?
) {

  /**
   * Returns the label data with the given color.
   *
   * Returns this if the color is the same
   */
  fun withColor(newColor: Color): LabelData {
    if (color == newColor) {
      return this
    }

    return copy(color = newColor)
  }

  init {
    require(text.isNotBlank()) {
      "LabelText must not be empty"
    }
  }
}
