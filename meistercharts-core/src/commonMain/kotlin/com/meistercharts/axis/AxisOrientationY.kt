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
package com.meistercharts.axis

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
