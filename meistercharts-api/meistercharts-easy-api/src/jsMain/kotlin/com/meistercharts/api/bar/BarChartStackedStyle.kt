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
package com.meistercharts.api.bar

import com.meistercharts.api.FontStyle
import com.meistercharts.api.Insets
import com.meistercharts.api.NumberFormat
import it.neckar.open.unit.other.px

/**
 * External configuration that can be used to configure the style of a stacked bar chart
 */
external interface BarChartStackedStyle : BarChartStyle {
  /**
   * The format to be used for values shown close to the bar segments
   */
  val valueFormat: NumberFormat?

  /**
   * The colors to be used for the segments of a stacked bar.
   * Every string must denote a valid CSS color definition.
   * If there are more segments than colors this array will be traversed again beginning with the first element (modulo).
   */
  val segmentColors: Array<String>?

  /**
   * The color to be used for the border of remainder segments.
   */
  val remainderSegmentBorderColor: String?

  /**
   * The width (in pixels) of the border of remainder segments.
   */
  val remainderSegmentBorderWidth: @px Double?

  /**
   * The color for the remainder segment background.
   *
   * Usually white for light themes
   */
  val remainderSegmentBackgroundColor: String?

  /**
   * The bar width (for the stacked bar)
   */
  val barWidth: @px Double?

  /**
   * The minimum distance between two consecutive bars. Center to center
   */
  val minBarDistance: @px Double?

  /**
   * The maximum distance between two consecutive bars. Center to center
   */
  val maxBarDistance: @px Double?

  /**
   * The font to be used for the value labels
   */
  val valueLabelFont: FontStyle?

  /**
   * The color to be used for the value labels; set to null to use the same color as the corresponding bar segment
   */
  val valueLabelColor: String?
}
