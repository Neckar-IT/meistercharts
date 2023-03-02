package com.meistercharts.algorithms.axis

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
