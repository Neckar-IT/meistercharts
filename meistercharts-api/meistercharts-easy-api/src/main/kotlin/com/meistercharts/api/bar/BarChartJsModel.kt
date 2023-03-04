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
external interface BarChartData : CategoriesSeriesData {
  //BarChartData sounds nicer than CategoriesData. This interface ensures the nicer name in the external JavaScript API
}

/**
 * Contains style attributes for all bar charts (stacked and grouped)
 */
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

