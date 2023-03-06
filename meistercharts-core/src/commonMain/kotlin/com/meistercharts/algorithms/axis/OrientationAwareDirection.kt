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
