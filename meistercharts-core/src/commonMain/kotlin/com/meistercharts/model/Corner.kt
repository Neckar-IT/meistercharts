package com.meistercharts.model

/**
 * Describes a corner
 */
enum class Corner {
  TopRight,
  BottomRight,
  BottomLeft,
  TopLeft;

  companion object {
    /**
     * Returns the corner for a given index.
     * This method is (a little bit) faster then `values[index]`
     */
    fun get(index: Int): Corner {
      return when (index) {
        0    -> TopRight
        1    -> BottomRight
        2    -> BottomLeft
        3    -> TopLeft
        else -> throw IllegalArgumentException("Invalid index <$index>")
      }
    }
  }
}
