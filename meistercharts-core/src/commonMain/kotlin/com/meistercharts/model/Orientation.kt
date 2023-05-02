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
package com.meistercharts.model

/**
 * The orientation within the chart.
 */
enum class Orientation {
  /**
   * The values of the chart are visible on the vertical (y) axis.
   * e.g. BarChart: Bars are painted bottom to top
   */
  Vertical,

  /**
   * The values of the chart are visible on the horizontal (x) axis.
   * e.g. BarChart: Bars are painted left to right
   */
  Horizontal;

  /**
   * Returns the opposite orientation
   */
  fun opposite(): Orientation {
    return when (this) {
      Vertical -> Horizontal
      Horizontal -> Vertical
    }
  }

  fun isHorizontal(): Boolean {
    return this == Horizontal
  }

  fun isVertical(): Boolean {
    return this == Vertical
  }
}
