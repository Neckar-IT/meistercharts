package com.meistercharts.algorithms.axis

import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.VerticalAlignment

/**
 * Describes a direction that is aware of the orientation of the axis
 */
enum class OrientationAwareDirection {
  /**
   * Direction pointing towards smaller values
   */
  TowardsSmaller,

  /**
   * In neither direction
   */
  Center,

  /**
   * Direction pointing towards larger values
   */
  TowardsLarger,
  ;

  companion object {
    /**
     * Calculate the (abstract) direction from an axis orientation and alignment
     */
    fun Companion.calculate(axisOrientation: AxisOrientationX, horizontalAlignment: HorizontalAlignment): OrientationAwareDirection {
      return when (axisOrientation) {
        AxisOrientationX.OriginAtLeft -> {
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> TowardsSmaller
            HorizontalAlignment.Right -> TowardsLarger
            HorizontalAlignment.Center -> Center
          }
        }
        AxisOrientationX.OriginAtRight -> {
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> TowardsLarger
            HorizontalAlignment.Right -> TowardsSmaller
            HorizontalAlignment.Center -> Center
          }
        }
      }
    }

    /**
     * Calculate the (abstract) direction from an axis orientation and alignment
     */
    fun Companion.calculate(axisOrientation: AxisOrientationY, verticalAlignment: VerticalAlignment): OrientationAwareDirection {
      return when (axisOrientation) {
        AxisOrientationY.OriginAtTop -> {
          when (verticalAlignment) {
            VerticalAlignment.Top -> TowardsSmaller
            VerticalAlignment.Bottom -> TowardsLarger
            VerticalAlignment.Center -> Center
            VerticalAlignment.Baseline -> Center
          }
        }
        AxisOrientationY.OriginAtBottom -> {
          when (verticalAlignment) {
            VerticalAlignment.Top -> TowardsLarger
            VerticalAlignment.Bottom -> TowardsSmaller
            VerticalAlignment.Center -> Center
            VerticalAlignment.Baseline -> Center
          }
        }
      }
    }
  }
}
