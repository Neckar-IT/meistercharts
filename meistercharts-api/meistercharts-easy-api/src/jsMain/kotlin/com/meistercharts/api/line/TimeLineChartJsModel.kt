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
package com.meistercharts.api.line

import com.meistercharts.annotations.WindowRelative
import com.meistercharts.api.BoxStyle
import com.meistercharts.api.CrossWireStyle
import com.meistercharts.api.DataSeriesNumberFormat
import com.meistercharts.api.EnumAggregationMode
import com.meistercharts.api.EnumAxisStyle
import com.meistercharts.api.EnumConfiguration
import com.meistercharts.api.FontStyle
import com.meistercharts.api.LineStyle
import com.meistercharts.api.NumberFormat
import com.meistercharts.api.PointType
import com.meistercharts.api.StripeStyle
import com.meistercharts.api.Threshold
import com.meistercharts.api.TimeAxisStyle
import com.meistercharts.api.TimeRange
import com.meistercharts.api.ValueAxisStyle
import com.meistercharts.api.ValueRange
import com.meistercharts.history.DecimalDataSeriesIndexInt
import com.meistercharts.history.EnumDataSeriesIndexInt
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.s

/**
 * The model of the time-line chart
 */
@JsExport
external interface TimeLineChartData {
  /**
   * The settings for the history
   */
  val historySettings: HistorySettings?

  /**
   * Whether the play mode is active
   */
  val play: Boolean?
}

@JsExport
enum class DownSamplingMode {
  /**
   * Down sampling is executed automatically
   */
  Automatic,

  /**
   * Down sampling is not scheduled. It is probably necessary to calculate it manually.
   */
  None,
}

/**
 * The settings for the history
 */
@JsExport
external interface HistorySettings {

  /**
   * The down sampling mode.
   *
   * Default: [DownSamplingMode.Automatic].
   */
  val downSamplingMode: DownSamplingMode?

  /**
   * The expected time between two consecutive samples.
   */
  val durationBetweenSamples: @ms Double?

  /**
   * This factor is used to calculate the minimal distance between two data points that will be interpreted as gap.
   *
   * The minimal gap distance ist calculated by multiplying [durationBetweenSamples] with the given factor.
   * If the distance between two data points is smaller than the minimal gap distance, the two data points are connected by a line.
   * If the distance is larger, it is assumed that there is a gap and the data points are not connected.
   *
   * Suggested value range: around 3.0 - 7.0.
   */
  val minGapSizeFactor: Double?

  /**
   * The history guarantees to store samples for at least this length (in seconds) at [durationBetweenSamples].
   */
  val guaranteedHistoryLength: @s Int
}

/**
 * Configuration for a decimal data series
 */
@JsExport
external interface DecimalDataSeries {
  /**
   * The id of the data series
   */
  val id: Int

  /**
   * The name of the data series
   */
  val name: String
}

/**
 * Configuration for a enum data series
 */
@JsExport
external interface EnumDataSeries {
  /**
   * The id of the data series
   */
  val id: Int

  /**
   * The name of the data series
   */
  val name: String

  /**
   * The enum configuration
   */
  val enumConfiguration: EnumConfiguration
}

@JsExport
external interface DiscreteDataSeriesConfiguration {
  /**
   * The id of the data series
   */
  val id: Int

  /**
   * The name of the data series
   */
  val name: String

  /**
   * The status enum configuration - if there is one.
   */
  val statusEnumConfiguration: EnumConfiguration?

  /**
   * Defines the stripe styles for the ordinals of the discrete data series.
   * The index within the array corresponds to the ordinal value
   *
   * If null is provided within the array, a default fill will be used.
   */
  val stripeStyles: Array<StripeStyle?>?

  /**
   * The font to be used for a stripe-label
   */
  val stripeLabelFont: FontStyle?

  /**
   * The color that is used to separate the segments from each other
   */
  val stripeSegmentSeparatorColor: String?

  /**
   * The width of the separator between two segments
   */
  val stripeSegmentSeparatorWidth: Double?

  /**
   * How values are aggregated when the plotter is zoomed out
   */
  val aggregationMode: DiscreteEntryAggregationMode?

}

@JsExport
enum class DiscreteEntryAggregationMode{
  /**
   * Visualizes the most of the time reference entry
   */
  MostOfTheTimeLabel,

  /**
   * Visualizes the most important status.
   *
   * Does *not* show the most-of-the-time-label to avoid confusion.
   * It is possible that the most-of-the-time-label does not have the status enum with the lowest ordinal.
   */
  MostImportantStatus,
}

/**
 * Data measured at a certain point in time
 */
@JsExport
external interface Sample {
  /**
   * A timestamp in milliseconds since 1 January 1970 UTC
   */
  val timestamp: @ms Double

  /**
   * The value for each line at the timestamp of this sample.
   *
   * [Double.NaN] represents "No value"
   */
  @Suppress("ArrayPrimitive")
  val decimalValues: Array<Double>?

  /**
   * The min value for each line at the timestamp of this sample
   * [Double.NaN] represents "No value"
   */
  @Suppress("ArrayPrimitive")
  val decimalMinValues: Array<Double>?

  /**
   * The max value for each line at the timestamp of this sample
   * [Double.NaN] represents "No value"
   */
  @Suppress("ArrayPrimitive")
  val decimalMaxValues: Array<Double>?

  /**
   * The ordinal values for each enumeration at the timestamp of this sample
   *
   * [Double.NaN] represents "No value"
   */
  @Suppress("ArrayPrimitive")
  val enumValues: Array<Double>? //must be double since JS does not support Int
}

@JsExport
external interface TimeLineChartStyle {
  /**
   * Whether to show the toolbar (true, default) or not (false)
   *
   */
  val showToolbar: Boolean?

  /**
   * Whether to show the hint on mouse wheel events without any modifier (true, default) or not (false)
   */
  val showMouseWheelModifierHint: Boolean?

  /**
   * The time range that is currently visible (in UTC)
   */
  val visibleTimeRange: TimeRange?

  /**
   * The position of the cross wire along the x-axis relative to the width of the window
   */
  val crossWirePosition: @WindowRelative Double?

  /**
   * Style for the cross-wire
   */
  val crossWireStyle: CrossWireStyle?

  /**
   * The configurations of each line
   */
  val decimalDataSeriesStyles: Array<DecimalDataSeriesStyle>?

  /**
   * The configurations of each enum data series
   */
  val enumDataSeriesStyles: Array<EnumDataSeriesStyle>?

  /**
   * How the lines should be painted.
   * This information is used in a modulo-fashion.
   */
  val lineStyles: Array<TimeLineChartLineStyle>?

  /**
   * The indices of the visible (decimal) lines.
   * Set to `[-1]` in order to make all lines visible.
   */
  @Suppress("ArrayPrimitive")
  val visibleLines: Array<@DecimalDataSeriesIndexInt Int>?

  /**
   * The indices of the visible enum stripes.
   * Set to `[-1]` in order to make all stripes visible.
   */
  @Suppress("ArrayPrimitive")
  val visibleEnumStripes: Array<@EnumDataSeriesIndexInt Int>?

  /**
   * The indices of the visible value axes.
   * Set to `[-1]` in order to make the value-axes of alle lines visible.
   */
  @Suppress("ArrayPrimitive")
  val visibleValueAxes: Array<@DecimalDataSeriesIndexInt Int>?

  /**
   * The color to be used for the background of the value axes
   */
  val valueAxesBackground: String?

  /**
   * The gap between each value axes in pixels
   */
  val valueAxesGap: @px Double?

  /**
   * The style to be used for the value axes
   */
  val valueAxesStyle: ValueAxisStyle?

  /**
   * The style to be used for the time axis
   */
  val timeAxisStyle: TimeAxisStyle?

  /**
   * The font to be used for the cross wire
   */
  val crossWireFont: FontStyle?

  /**
   * How the boxes for the cross wire labels should be painted (for decimal values).
   */
  val crossWireDecimalsLabelBoxStyles: Array<BoxStyle>?

  /**
   * The format to be used for the labels at the cross wire.
   */
  val crossWireDecimalsFormat: DataSeriesNumberFormat?

  /**
   * The color for the cross wire value labels
   */
  val crossWireDecimalsLabelTextColor: String?

  /**
   * How the boxes for the cross wire labels should be painted (for enum values).
   */
  val crossWireEnumsLabelBoxStyles: Array<BoxStyle>?

  /**
   * The style for the enum layers axis
   */
  val enumAxisStyle: EnumAxisStyle?

  /**
   * The height of an enum stripe
   */
  val enumStripeHeight: Double?

  /**
   * The distance between enum stripes
   */
  val enumsStripesDistance: Double?

  /**
   * The background color of the area that contains the enums
   */
  val enumsBackgroundColor: String?
}

/**
 * The configuration of a line of a time-line chart
 * For decimal values - use [EnumDataSeriesStyle] for enum values.
 */
@JsExport
external interface DecimalDataSeriesStyle {
  /**
   * The axis title of the series
   */
  val valueAxisTitle: String?

  /**
   * The value range to be used to scale the y-axis for this line.
   */
  val valueRange: ValueRange?

  /**
   * The format to be used for the tick values
   */
  val ticksFormat: NumberFormat?

  /**
   * The (optional) thresholds for this data series.
   * This object also contains the value for the threshold (which should not change very often)
   *
   * The thresholds are only visible, if the value axis for this decimal data series is visible.
   */
  val thresholds: Array<Threshold>?
}

/**
 * The config of an enum-data series in the Plotter
 */
@JsExport
external interface EnumDataSeriesStyle {
  /**
   * Defines the stripe styles for the ordinals of the enum.
   * The index within the array corresponds to the ordinal value
   *
   * If null is provided within the array, a default fill will be used.
   */
  val stripeStyles: Array<StripeStyle?>?

  /**
   * How enum-values are aggregated when the plotter is zoomed out
   * Default: 'winner-takes-all'
   */
  val aggregationMode: EnumAggregationMode?
}

/**
 * Configuration for a single (decimal) line in the timeline chart.
 */
@JsExport
external interface TimeLineChartLineStyle {
  /**
   * The line style
   */
  val lineStyle: LineStyle

  /**
   * How the points are painted - default is [PointType.None]
   * Uses the line color.
   */
  val pointType: PointType?

  /**
   * The size of the point in pixels - depending on the [pointType].
   * Usually the diameter. Default: 2.0
   */
  val pointSize: Double?

  //Currently, we do not support custom colors and styles
  ///**
  // * The line-width to be used for a point; only relevant for cross and cross-45-degrees like points
  // */
  //val pointLineWidth: Double?
  //
  ///**
  // * The first color to be used for a point
  // */
  //val pointColor1: String?
  //
  ///**
  // * The second color to be used for a point
  // */
  //val pointColor2: String?


  /**
   * The color for the min/max area
   */
  val minMaxAreaColor: String?

  /**
   * If set to true the min/max area is visible, default is false
   */
  val showMinMaxArea: Boolean?

}
