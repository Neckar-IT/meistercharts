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

import com.meistercharts.api.BalloonTooltipStyle
import com.meistercharts.api.Insets
import com.meistercharts.api.OverflowIndicatorStyle
import com.meistercharts.api.Threshold
import com.meistercharts.api.ValueLabelsStyle
import it.neckar.open.unit.other.px

/**
 * The data model of the grouped bar chart
 */
@JsExport
external interface BarChartGroupedData : BarChartData {
}

/**
 * External configuration that can be used to configure the style of a grouped bar chart
 */
@JsExport
external interface BarChartGroupedStyle : BarChartStyle {
  /**
   * The colors to be used for the bars of a group.
   * Every string must denote a valid CSS color definition.
   *
   * If there are more groups than the size of the array, the array will be traversed again from the beginning (modulo).
   *
   * If there are more bars than colors in a group that array will also be traversed again from the beginning (modulo).
   *
   * The first dimension denotes the group, the second the bar.
   */
  val barColors: Array<Array<String?>?>?

  /**
   * The width (in pixels) of a bar if the chart is vertical or the height (in pixels) of a bar if the chart is horizontal
   */
  val barSize: @px Double?

  /**
   * The distance between two bars (within one group) in pixels
   */
  val barGap: @px Double?

  /**
   * The minimum distance between two consecutive groups in pixels. Space *between* the groups - not center to center.
   */
  val minGapBetweenGroups: @px Double?

  /**
   * The maximum distance between two consecutive groups in pixels. Space *between* the groups - not center to center.
   */
  val maxGapBetweenGroups: @px Double?

  /**
   * The optional array of thresholds
   */
  val thresholds: Array<Threshold>?

  /**
   * The style of the value labels
   */
  val valueLabelsStyle: ValueLabelsStyle?

  /**
   * Whether to show tooltips
   */
  val showTooltip: Boolean?

  /**
   * Style for the tool tips
   */
  val tooltipStyle: BalloonTooltipStyle?

  /**
   * The background-color to be used for the active group.
   */
  val activeGroupBackgroundColor: String?

  /**
   * Style for the overflow indicator
   */
  val overflowIndicatorStyle: OverflowIndicatorStyle?
}
