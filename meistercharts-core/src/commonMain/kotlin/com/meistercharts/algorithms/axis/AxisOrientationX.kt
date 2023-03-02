package com.meistercharts.algorithms.axis

/**
 * The orientation of the axis
 *
 */
enum class AxisOrientationX : AxisInversionInformation {
  /**
   * Smallest domain value at left, positive domain values correspond to *positive* pixel values
   */
  OriginAtLeft,

  /**
   * Smallest domain value at right, positive domain values correspond to *negative* pixel values
   */
  OriginAtRight;

  /**
   * Returns the opposite
   */
  fun opposite(): AxisOrientationX {
    return when (this) {
      OriginAtLeft -> OriginAtRight
      OriginAtRight -> OriginAtLeft
    }
  }

  /**
   * Returns the opposite if the given boolean is true
   */
  fun oppositeIf(takeOpposite: Boolean): AxisOrientationX {
    return if (takeOpposite) {
      opposite()
    } else {
      this
    }
  }

  /**
   * Returns true if the axis is inverted (not from left to right as usually expected from the x axis)
   */
  override val axisInverted: Boolean
    get() = this == OriginAtRight
}
