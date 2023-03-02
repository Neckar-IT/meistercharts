package com.meistercharts.algorithms.axis

/**
 * The orientation of the axis
 *
 */
enum class AxisOrientationY : AxisInversionInformation {
  /**
   * Smallest domain value at bottom, positive domain values correspond to *negative* pixel values
   */
  OriginAtBottom,

  /**
   * Smallest domain value at top, positive domain values correspond to *positive* pixel values
   */
  OriginAtTop;

  /**
   * Returns true if the axis is inverted (not from bottom to top as usually expected from the y axis)
   */
  override val axisInverted: Boolean
    get() = this == OriginAtBottom


  /**
   * Returns the opposite
   */
  fun opposite(): AxisOrientationY {
    return when (this) {
      OriginAtBottom -> OriginAtTop
      OriginAtTop -> OriginAtBottom
    }
  }

  /**
   * Returns the opposite if the given boolean is true
   */
  fun oppositeIf(takeOpposite: Boolean): AxisOrientationY {
    return if (takeOpposite) {
      opposite()
    } else {
      this
    }
  }
}
