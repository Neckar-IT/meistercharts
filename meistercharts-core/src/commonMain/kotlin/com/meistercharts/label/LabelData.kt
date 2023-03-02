package com.meistercharts.label

import com.meistercharts.algorithms.painter.Color

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
