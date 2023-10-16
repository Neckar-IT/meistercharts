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

import com.meistercharts.api.CategoryAxisStyle
import com.meistercharts.api.GridStyle
import com.meistercharts.api.HasContentViewportMargin
import com.meistercharts.api.ValueAxisStyle
import com.meistercharts.api.ValueRange
import com.meistercharts.api.category.CategoriesSeriesData

/**
 * Contains all external interfaces that represent the API towards the browser.
 * There is no guaranty that we will receive non-null and well-defined values. Hence, all types are nullable.
 */

/**
 * The data model of the bar charts (stacked or grouped)
 */
@JsExport
external interface BarChartData : CategoriesSeriesData {
  //BarChartData sounds nicer than CategoriesData. This interface ensures the nicer name in the external JavaScript API
}

/**
 * Contains style attributes for all bar charts (stacked and grouped)
 */
@JsExport
external interface BarChartStyle : HasContentViewportMargin {
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
   * The style to be used for the grid
   */
  val gridStyle: GridStyle?

  /**
   * The value range to be used
   */
  val valueRange: ValueRange?
}

