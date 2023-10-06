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
package com.meistercharts.api.bullet

import com.meistercharts.api.BalloonTooltipStyle
import com.meistercharts.api.CategoryAxisStyle
import com.meistercharts.api.GridStyle
import com.meistercharts.api.Insets
import com.meistercharts.api.OverflowIndicatorStyle
import com.meistercharts.api.Threshold
import com.meistercharts.api.ValueAxisStyle
import com.meistercharts.api.ValueRange
import com.meistercharts.api.category.CategoryBulletChartData
import it.neckar.open.unit.other.px


/**
 * The configuration of the bullet chart
 */
@JsExport
external interface BulletChartConfiguration {
  /**
   * The categories to be shown.
   */
  val categories: Array<CategoryBulletChartData>?

  /**
   * The colors to be used for the bar
   * Every string must denote a valid CSS color definition.
   *
   * If there are more area bars than the size of the array, the array will be traversed again from the beginning (modulo).
   */
  val barColors: Array<String>?

  /**
   * The color for the current value indicator.
   * Must denote a valid CSS color definition.
   */
  val currentValueIndicatorColor: String?

  /**
   * The color for the current value indicator.
   * Must denote a valid CSS color definition.
   */
  val currentValueIndicatorOutlineColor: String?

  /**
   * Whether the chart is horizontal (true) or vertical (false)
   */
  val horizontal: Boolean?

  /**
   * The style of the category axis
   */
  val categoryAxisStyle: CategoryAxisStyle?

  /**
   * The style of the value axis
   */
  val valueAxisStyle: ValueAxisStyle?

  /**
   * The style to be used for the grid lines associated with the categories
   */
  val categoriesGridStyle: GridStyle?

  /**
   * The style to be used for the grid lines associated with the value axis
   */
  val valuesGridStyle: GridStyle?

  /**
   * The value range to be used
   */
  val valueRange: ValueRange?

  /**
   * The width (in pixels) of the bar.
   * Height: for horizontal areas
   * Width: for vertical areas
   */
  val barSize: @px Double?

  /**
   * The width/height of the current value indicator
   */
  val currentValueIndicatorSize: @px Double?

  /**
   * The min gap between two consecutive categories in pixels. Space *between* the categories - not center to center.
   */
  val minGapBetweenCategories: @px Double?

  /**
   * The min gap between two consecutive categories in pixels. Space *between* the categories - not center to center.
   */
  val maxGapBetweenCategories: @px Double?

  /**
   * The optional thresholds of the chart
   */
  val thresholds: Array<Threshold>?

  /**
   * Whether to show tooltips
   */
  val showTooltip: Boolean?

  /**
   * Style for the tool tips.
   *
   * The tooltip style must contain exactly two symbol sizes.
   * * The first one is interpreted as size for the current value
   * * The second one is interpreted as size for the area
   */
  val tooltipStyle: BalloonTooltipStyle?

  /**
   * The background-color to be used for the active category.
   */
  val activeCategoryBackgroundColor: String?

  /**
   * The content viewport margin: The space around the content viewport.
   */
  val contentViewportMargin: @px Insets?

  /**
   * The style of the overflow indicators (e.g. for bars)
   */
  val overflowIndicatorStyle: OverflowIndicatorStyle?
}


