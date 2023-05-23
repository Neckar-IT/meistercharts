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
package com.meistercharts.api

import com.meistercharts.annotations.Domain
import com.meistercharts.history.DecimalDataSeriesIndexInt
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Contains default models
 */

/**
 * Tooltip style for balloon tool tips
 */
external interface BalloonTooltipStyle {
  /**
   * The font style for the headline (if there is any)
   */
  val headlineFont: FontStyle?

  /**
   * The gap between the headline and the entries
   */
  val headlineMarginBottom: Double?

  /**
   * The style for the balloon tooltip
   */
  val tooltipBoxStyle: BoxStyle?

  /**
   * The font to be used for the tooltips
   */
  val tooltipFont: FontStyle?

  /**
   * The format to be used for values of the tooltips
   */
  val tooltipFormat: NumberFormat?

  /**
   * The (vertical) gap between the entries
   */
  val entriesGap: Double?

  /**
   * The gap between the symbol and the label
   */
  val symbolLabelGap: Double?

  /**
   * The (max) sizes of the symbols.
   *
   * Depending on the context these sizes are interpreted differently.
   * Usually, the sizes are applied using modulo.
   */
  val symbolSizes: Array<Size>?

  /**
   * The max width of the label (without symbol, paddings, margins...)
   */
  val labelWidth: Double?
}

/**
 * Describes how an overflow indicator is styled
 */
external interface OverflowIndicatorStyle {
  /**
   * The fill of the indicator
   */
  val fill: String?

  /**
   * The stroke. Either a fill or a stroke must be provided
   */
  val stroke: String?

  /**
   * The line width of the stroke.
   * If not set a default of 1.0 will be used.
   */
  val strokeWidth: Double?

  /**
   * The width of the arrow head (default: 15px)
   */
  val arrowHeadWidth: Double

  /**
   * The length of the arrow head (default: 10px)
   */
  val arrowHeadLength: Double
}

/**
 * Describes how a cross-wire should look like
 */
external interface CrossWireStyle {
  /**
   * The color of the cross-wire line
   */
  val wireColor: String?

  /**
   * The width (in pixels) of the cross-wire line
   */
  val wireWidth: Double?
}

/**
 * Represents a size
 */
external interface Size {
  /**
   * The width (in pixels)
   */
  val width: Double?

  /**
   * The height (in pixels)
   */
  val height: Double?
}

/**
 * Represents insets.
 * If some values are not set they might be interpreted as 0.0 or ignored.
 */
external interface Insets {
  val top: Double?
  val left: Double?
  val right: Double?
  val bottom: Double?
}

/**
 * Represents a value range
 */
external interface ValueRange {
  /**
   * The start of the range (inclusive)
   */
  val start: Double

  /**
   * The end of the range (inclusive)
   */
  val end: Double

  /**
   * The scale to be for this value range; default is [ValueRangeScale.Linear]
   */
  val scale: ValueRangeScale?
}

enum class ValueRangeScale {
  /**
   * Denotes a linear value range.
   */
  Linear,

  /**
   * Denotes a logarithmic value range for a logarithm to base 10
   */
  Log10
}

/**
 * Represents a time range
 */
external interface TimeRange {
  /**
   * The start of the time range in milliseconds since 1 January 1970 UTC
   */
  val start: @ms Double

  /**
   * The end of the time range in milliseconds since 1 January 1970 UTC
   */
  val end: @ms Double
}

/**
 * Represents a pair of zoom for both axis
 *
 * * Values greater than 1.0 generally indicate a zoomed in state
 * * Values smaller than 1.0 generally indicate a zoomed out state
 */
external interface Zoom {
  /**
   * The scale factor along the x-axis
   */
  val scaleX: Double?

  /**
   * The scale factor along the y-axis
   */
  val scaleY: Double?
}

/**
 * A side of a four-sided shape.
 */
enum class Side {
  /**
   * The left side
   */
  Left,

  /**
   * The right side
   */
  Right,

  /**
   * The top side
   */
  Top,

  /**
   * The bottom side
   */
  Bottom
}

/**
 * Describes the distance between two data points.
 */
enum class SamplingPeriod {
  /**
   * 1 sample every millisecond
   */
  EveryMillisecond,

  /**
   * 1 sample every 10 milliseconds
   */
  EveryTenMillis,

  /**
   * 1 sample every 100 milliseconds
   */
  EveryHundredMillis,

  /**
   * 1 sample per second
   */
  EverySecond,

  /**
   * 1 sample per 10 seconds
   */
  EveryTenSeconds,

  /**
   * 1 sample per minute
   */
  EveryMinute,

  /**
   * 1 sample per 10 minutes
   */
  EveryTenMinutes,

  /**
   * 1 sample per hour
   */
  EveryHour,

  /**
   * 1 sample every 6 hours
   */
  Every6Hours,

  /**
   * 1 sample every 24 hours
   */
  Every24Hours
}

/**
 * How a line should be painted
 */
external interface LineStyle {
  /**
   * The width of the line
   */
  val width: Double?

  /**
   * The color of the line
   */
  val color: String?

  /**
   * How the connection between two points is painted
   */
  val type: PointConnectionStyle?
}

/**
 * How a (text) box should be painted
 */
external interface BoxStyle {
  /**
   * The padding of the box (on all four side) in pixels.
   * If this value is null, the default padding is used
   */
  val padding: Insets?

  /**
   * The color of the background fill.
   * null is interpreted as *no fill*
   */
  val backgroundColor: String?

  /**
   * The color of the foreground/text.
   * If this value is null the default foreground color is used.
   */
  val color: String?

  /**
   * The color of the border (or null if there is no border)
   * null is interpreted as *no border*
   */
  val borderColor: String?

  /**
   * The shadow.
   * null is interpreted as *no shadow*
   */
  val shadow: Shadow?

  /**
   * The border-radius. If set, these values are used to calculate rounded corners for the box.
   *
   * null is interpreted as *no rounded corners
   */
  val borderRadius: BorderRadius?
}

/**
 * Describes a shadow (e.g. for a box style)
 */
external interface Shadow {
  /**
   * The color of the shadow.
   * Default: black
   */
  val color: String?

  /**
   * Default: 10.0
   */
  val blurRadius: @px Double?

  /**
   * Default: 0.0
   */
  val offsetX: @px Double?

  /**
   * Default: 0.0
   */
  val offsetY: @px Double?
}

/**
 * Describes the radii for rounded rectangles.
 *
 * null values are interpreted as "0.0"
 * negative values are not allowed
 */
external interface BorderRadius {
  @PositiveOrZero
  val topLeft: Double?

  @PositiveOrZero
  val topRight: Double?

  @PositiveOrZero
  val bottomRight: Double?

  @PositiveOrZero
  val bottomLeft: Double?
}


/**
 * How points are connected
 */
enum class PointConnectionType {
  /**
   * Points are connected with a direct line
   */
  Direct,

  /**
   * Points are connected using a spline
   */
  Spline

}

/**
 * Describes how the connection between two points is painted
 */
enum class PointConnectionStyle {
  /**
   * No line at all
   */
  None,

  /**
   * A continuous solid line
   */
  Continuous,

  /**
   * A dotted line
   */
  Dotted,

  /**
   * A line made of small dashes
   */
  SmallDashes,

  /**
   * A line made of large dashes
   */
  LargeDashes
}

/**
 * Describes how a point looks like
 */
enum class PointType {
  /**
   * No point at all
   */
  None,

  /**
   * A simple dot
   */
  Dot,

  /**
   * A cross
   */
  Cross,

  /**
   * A cross rotated by 45 degrees
   */
  Cross45,

  /**
   * A potentially filled circle
   */
  Circle
}

/**
 * How to wrap lines
 */
enum class WrapMode {
  /**
   * Keep the text on a single line.
   * Do not wrap
   */
  NoWrap,

  /**
   * Split the text if necessary
   */
  IfNecessary
}

/**
 * Describes the font style
 */
external interface FontStyle {
  /**
   * The font-family (see https://developer.mozilla.org/en-US/docs/Web/CSS/font-family)
   */
  val family: String?

  /**
   * The font style (see https://developer.mozilla.org/en-US/docs/Web/CSS/font-style)
   */
  val style: String?

  /**
   * The font-size (in pixels) (see https://developer.mozilla.org/en-US/docs/Web/CSS/font-size)
   */
  val size: @px Double?

  /**
   * The font-weight (see https://developer.mozilla.org/en-US/docs/Web/CSS/font-weight)
   */
  val weight: Int?
}

/**
 * Contains common chart style settings
 */
external interface ChartStyle {
  /**
   * The target refresh rate. 0 means unlimited which is the default.
   *
   * E.g. a target refresh rate of 5 means that the canvas is repainted at most 5 times a second.
   */
  val targetRefreshRate: Int?
}

/**
 * Defines how an axis looks like and behaves
 */
external interface AxisStyle {
  /**
   * The side on which to put the axis
   */
  val axisSide: Side?

  /**
   * The width of the axis (if placed on the left or right side of a chart) or the height of the axis (if placed on the top or bottom side of a chart)
   */
  val axisSize: @px Double?

  /**
   * The font to be used for the ticks
   */
  val tickFont: FontStyle?

  /**
   * The color to be used for the tick labels
   */
  val tickLabelColor: String?

  /**
   * The length of a tick
   */
  val tickLength: @px Double?

  /**
   * The gap between the end of an axis ticks and its label
   */
  val tickLabelGap: @px Double?

  /**
   * The color of the axis line
   */
  val axisLineColor: String?

  /**
   * The width of the axis line
   */
  val axisLineWidth: @px Double?

  /**
   * The title of the axis
   */
  val title: String?

  /**
   * The space that is added to the width (vertical axis) or height (horizontal axis) of the text box of the title
   */
  val titleGap: @px Double?

  /**
   * The color of the axis title
   */
  val titleColor: String?

  /**
   * The font of the axis title
   */
  val titleFont: FontStyle?
}

/**
 * Defines how a value axis looks like and behaves
 */
external interface ValueAxisStyle : AxisStyle {
  /**
   * The maximum number of ticks for the axis
   */
  val maxTickCount: Int?

  /**
   * The format to be used for the tick values
   */
  val ticksFormat: NumberFormat?

  /**
   * How the value axis is presented
   */
  val presentationType: ValueAxisPresentationType?
}

enum class ValueAxisPresentationType {
  /**
   * Default style with axis line, ticks and labels
   */
  Default,

  /**
   * Special case: Only show the "0"
   * No ticks or axis line
   */
  Only0,
}

/**
 * Defines how a category axis looks like and behaves
 */
external interface CategoryAxisStyle : AxisStyle {
  /**
   * The minimum distance (in pixels) between two tick labels or tick icons
   */
  val minTickLabelDistance: @px Double?

  /**
   * The size of a category icon
   */
  val iconSize: @px Double?

  /**
   * Determines how labels and icons associated with a tick are laid out
   */
  val justifyTickContent: JustifyTickContent?
}

/**
 * Determines how labels and icons associated with a tick are laid out
 */
enum class JustifyTickContent {
  /**
   * Space is distributed greedily from left to right (horizontal axis) or from top to bottom (vertical axis).
   *
   * If a tick label and/or icon does not fit into its designated bounding box the space of the bounding box is used for the next tick.
   *
   * A tick label is always fully painted or not painted at all.
   */
  SpaceGreedily,

  /**
   * Space is distributed greedily from left to right (horizontal axis) or from top to bottom (vertical axis).
   * This strategy prefers "round" tick indices (e.g. 10, 20) depending on the available space and label width.
   *
   * A tick label is always fully painted or not painted at all.
   */
  SpaceGreedilyPreferRoundIndices,

  /**
   * Every tick gets the same amount of space along the category axis.
   *
   * If the space does not suffice for the tick label to be fully painted the label is shortened usually with an ellipsis (...).
   */
  SpaceEvenly,
}

/**
 * Axis style for enum axis
 */
external interface EnumAxisStyle : AxisStyle {
  /**
   * If the labels are/will be wrapped
   */
  val labelWrapMode: WrapMode?
}

/**
 * Axis style fron discrete axis
 */
external interface DiscreteAxisStyle : AxisStyle {
  /**
   * If the labels are/will be wrapped
   */
  val labelWrapMode: WrapMode?

  /**
   * The background-color of this axis
   */
  val backgroundColor: String?
}

/**
 * Defines how a time axis looks like and behaves
 */
external interface TimeAxisStyle : AxisStyle {
  /**
   * The height (horizontal axis) or width (vertical axis) of the offset area
   */
  val offsetAreaSize: @px Double?

  /**
   * The color to be used for the tick labels displayed in the offset area
   */
  val offsetTickLabelColor: String?

  /**
   * The colors to be used for the offset area.
   * Every string must denote a valid CSS color definition.
   * If there are more offsets than colors this array will be traversed again beginning with the first element (modulo).
   */
  val offsetAreaFills: Array<String>?

  /**
   * The gap between the tick label and the offset area
   */
  val offsetAreaTickLabelGap: @px Double?
}

/**
 * Formats numbers
 */
external interface NumberFormat {
  /**
   * Formats the given value using the given locale.
   */
  fun format(value: Double, locale: String): String
}

/**
 * Formats ticks
 */
@Deprecated("No longer used/required. Use a NumberFormat instead")
external interface TicksFormat {
  /**
   * Formats the values contained in ticksIn using the given locale and stores them in ticksOut.
   */
  @Suppress("ArrayPrimitive")
  fun format(ticksIn: Array<Double>, ticksOut: Array<String>, locale: String)
}

/**
 * Format numbers of a data-series
 */
external interface DataSeriesNumberFormat {
  /**
   * Formats the given value of the date-series with the given index using the given locale.
   */
  fun format(index: @DecimalDataSeriesIndexInt Int, value: Double, locale: String): String
}

/**
 * Describes the look of a grid
 */
external interface GridStyle {
  /**
   * Provides colors for grid lines
   */
  val lineColors: GridLineColorProvider?

  /**
   * Whether the grid is visible (true) or not (false).
   */
  val visible: Boolean?
}

/**
 * Provides the color of a grid line
 */
external interface GridLineColorProvider {
  /**
   * Provides the color of the grid line at the given index or for the given value
   */
  fun lineColor(value: Double): String
}

/**
 * Threshold
 */
external interface Threshold {
  /**
   * The threshold value.
   * The value is only used to position the threshold line.
   */
  val value: @Domain Double

  /**
   * The threshold label (supports \n).
   * Usually contains at least the formatted value.
   */
  var label: String

  /**
   * The length of the arrow head that points from the label box to the axis.
   * Default is 10 pixels
   */
  var arrowHeadLength: @px Double

  /**
   * The width of the arrow head that points from the label box to the axis
   * Default is 10 pixels
   */
  var arrowHeadWidth: @px Double

  /**
   * The color to be used for the threshold label
   */
  val labelColor: String

  /**
   * The color to b eused for the threshold label if it is active
   */
  //TODO make non nullable!
  val labelColorActive: String?

  /**
   * The font to be used for the threshold label
   */
  val labelFont: FontStyle

  /**
   * The style to be used for the threshold line
   */
  val lineStyle: LineStyle

  /**
   * The style to be used for the threshold line - if it is active.
   * Is this property null, the [lineStyle] is used.
   */
  //TODO make non nullable!
  val lineStyleActive: LineStyle?

  /**
   * The box style for the label
   */
  val labelBoxStyle: BoxStyle

  /**
   * The box style for the label - if active
   * Is this property null, the [labelBoxStyle] is used.
   */
  //TODO make non nullable!
  val labelBoxStyleActive: BoxStyle?
}

/**
 * Describes the style to be used for value labels
 */
external interface ValueLabelsStyle {
  /**
   * Whether to show the value of a bar in a separate label
   */
  val showValueLabels: Boolean?

  /**
   * The distance between a value label and its corresponding bar in pixels - horizontal gap
   */
  val valueLabelGapHorizontal: @px Double?

  /**
   * The distance between a value label and its corresponding bar in pixels - vertical gap
   */
  val valueLabelGapVertical: @px Double?

  /**
   * The format to be used for the value-label of a bar
   */
  val valueLabelFormat: NumberFormat?

  /**
   * The font to be used for the value labels
   */
  val valueLabelFont: FontStyle?

  /**
   * The color to be used for the value labels; set to null to use the same color as the bar
   */
  val valueLabelColor: String?

  /**
   * The color to be used to stroke the value-labels
   */
  val valueLabelStrokeColor: String?
}

/**
 * Defines the style of a stripe (that might for example be used to visualize an enum)
 */
external interface StripeStyle {
  /**
   * The main background color
   */
  val backgroundColor: String?

  /**
   * The color to be used for a stripe-label
   */
  val labelColor: String?

  /**
   * An alternate color; how it is used depends on the {@link pattern}
   * (Default: none)
   */
  val alternateColor: String?

  /**
   * The color of the border
   * (Default: none)
   */
  val borderColor: String?

  /**
   * How the stripe is filled
   * (Default: Solid)
   */
  val fill: Fill?
}

/**
 * A fill style for a rectangle
 */
external enum class Fill {
  /**
   * The rectangle is filled with a solid color
   */
  Solid,

  //Horizontal,
  //Vertical,
  //ForwardDiagonal,
  //BackwardDiagonal,
  //DiagonalCross
}


/**
 * Base class for styles that have a content viewport margin
 */
external interface HasContentViewportMargin {
  /**
   * The content viewport margin: The space around the content viewport.
   */
  val contentViewportMargin: @px Insets?

}
