package com.meistercharts.api.line

import com.meistercharts.api.BalloonTooltipStyle
import com.meistercharts.api.CategoryAxisStyle
import com.meistercharts.api.CrossWireStyle
import com.meistercharts.api.GridStyle
import com.meistercharts.api.Insets
import com.meistercharts.api.LineStyle
import com.meistercharts.api.NumberFormat
import com.meistercharts.api.PointConnectionType
import com.meistercharts.api.PointType
import com.meistercharts.api.Threshold
import com.meistercharts.api.ValueAxisStyle
import com.meistercharts.api.ValueRange
import com.meistercharts.api.category.CategoriesSeriesData
import it.neckar.open.unit.other.px

/**
 * The data model of the simple line chart
 */
external interface LineChartSimpleData : CategoriesSeriesData {
}

/**
 * Contains style attributes for simple timeline charts
 */
external interface LineChartSimpleStyle {
  /**
   * The value range to be used
   */
  val valueRange: ValueRange?

  /**
   * The style of each line
   */
  val lineStyles: Array<LineChartLineStyle?>?

  /**
   * The minimum distance between two consecutive data points
   */
  val minDataPointDistance: @px Double?

  /**
   * The maximum distance between two consecutive data points
   */
  val maxDataPointDistance: @px Double?

  /**
   * The style used for the value axis
   */
  val valueAxisStyle: ValueAxisStyle?

  /**
   * The style used for the category axis
   */
  val categoryAxisStyle: CategoryAxisStyle?

  /**
   * The style to be used for the grid lines associated with the categories
   */
  val categoriesGridStyle: GridStyle?

  /**
   * The style to be used for the grid lines associated with the value axis
   */
  val valuesGridStyle: GridStyle?

  /**
   * The format to be used for all values of this chart
   */
  val valueFormat: NumberFormat?

  /**
   * The optional thresholds of the chart
   */
  val thresholds: Array<Threshold>?

  /**
   * Whether to show tooltips
   */
  val showTooltip: Boolean?

  /**
   * Style for the tool tips
   */
  val tooltipStyle: BalloonTooltipStyle?

  /**
   * Style for the tooltip-indicator line
   */
  val tooltipWireStyle: CrossWireStyle?

  /**
   * The indices of the visible lines.
   * Set to `[-1]` to make all lines visible.
   */
  @Suppress("ArrayPrimitive")
  val visibleLines: Array<Int>?

  /**
   * The content viewport margin: The space around the content viewport.
   */
  val contentViewportMargin: @px Insets?
}

/**
 * The style of a line of a line-chart
 */
external interface LineChartLineStyle {
  /**
   * Which point types to use
   */
  val pointType: Array<PointType?>?

  /**
   * The point sizes
   */
  @Suppress("ArrayPrimitive")
  val pointSize: Array<Double?>?

  /**
   * The line-width to be used for a point; only relevant for cross and cross-45-degrees like points
   */
  @Suppress("ArrayPrimitive")
  val pointLineWidth: Array<Double?>?

  /**
   * The first color to be used for a point
   */
  val pointColor1: Array<String?>?

  /**
   * The second color to be used for a point
   */
  val pointColor2: Array<String?>?

  /**
   * The line style
   */
  val lineStyle: LineStyle?

  /**
   * How points are connected
   */
  val pointConnectionType: PointConnectionType?
}
