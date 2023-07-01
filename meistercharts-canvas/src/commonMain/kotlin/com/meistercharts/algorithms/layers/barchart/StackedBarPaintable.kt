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
package com.meistercharts.algorithms.layers.barchart

import it.neckar.open.unit.number.Positive as Positive1
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.withCurrentChartState
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.axis.AxisOrientationX
import com.meistercharts.axis.AxisOrientationY
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.CanvasStringShortener
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.calculateOffsetXWithAnchor
import com.meistercharts.canvas.calculateOffsetYWithAnchor
import com.meistercharts.canvas.fillRoundedRect
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.strokeRoundedRect
import com.meistercharts.color.Color
import com.meistercharts.design.Theme
import com.meistercharts.geometry.Rectangle
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.model.Size
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.state.withContentAreaSize
import com.meistercharts.style.Palette.chartColors
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastFindAny
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.kotlin.lang.or0ifNanOrInfinite
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.Negative
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

/**
 * Paint values as a stacked bar
 */
class StackedBarPaintable(
  val data: Data = Data(),

  /**
   * The width
   */
  var width: @Zoomed Double,
  /**
   * The height of the bar
   */
  var height: @Zoomed Double,

  styleConfigurer: Style.() -> Unit = {}
) : Paintable {

  override fun boundingBox(paintingContext: LayerPaintingContext): @Zoomed Rectangle {
    val chartSupport = paintingContext.chartSupport

    @px val y: Double
    @px val x: Double

    when (style.orientation) {
      Orientation.Vertical -> {
        y = when (chartSupport.currentChartState.axisOrientationY) {
          AxisOrientationY.OriginAtBottom -> -height
          AxisOrientationY.OriginAtTop -> 0.0
        }

        x = -width / 2.0
      }

      Orientation.Horizontal -> {
        y = -height / 2.0
        x = when (chartSupport.currentChartState.axisOrientationX) {
          AxisOrientationX.OriginAtLeft  -> 0.0
          AxisOrientationX.OriginAtRight -> -width
        }
      }
    }

    return Rectangle(x, y, width, height)
  }

  val style: Style = Style().also(styleConfigurer)

  internal inner class Layout {
    /**
     * Returns true if there are positive and negative segments.
     * Also returns true if only the remainder is shown for positive or negative values.
     */
    val hasPositiveAndNegativeSegments: Boolean
      get() {
        return visibleSegmentsCountPositive > 0 && visibleSegmentsCountNegative > 0
      }

    val hasPositiveSegments: Boolean
      get() = segmentsSumPositive != 0.0

    val hasNegativeSegments: Boolean
      get() = segmentsSumNegative != 0.0

    /**
     * The number of segments that are visible (that have a value greater than 0 and greater than the start of the value range or are a remainder segment) - with positive values
     */
    var visibleSegmentsCountPositive = 0

    /**
     * The number of segments that are visible (that have a value less than 0 and less than the end of the value range or are a remainder segment) - with negative values
     */
    var visibleSegmentsCountNegative = 0

    /**
     * Returns the number of visible segments
     */
    val visibleSegments: Int
      get() {
        return visibleSegmentsCountPositive + visibleSegmentsCountNegative
      }

    /**
     * The sum of all domain values for all segments that are positive
     */
    var segmentsSumPositive: @Domain @Positive1 Double = 0.0

    /**
     * The sum of all domain values for all segments that are negative
     */
    var segmentsSumNegative: @Domain @Negative Double = 0.0

    /**
     * The total sum for the segments
     */
    val segmentsSum: @Domain @Positive1 Double
      get() {
        return segmentsSumNegative.abs() + segmentsSumPositive
      }

    var remainderPositiveVisible: Boolean = false
    var remainderNegativeVisible: Boolean = false

    /**
     * The start position of the positive remainder
     */
    var remainderStartPositive: @DomainRelative @Positive1 Double = Double.NaN

    /**
     * The end of the positive remainder
     */
    var remainderEndPositive: @DomainRelative @Positive1 Double = Double.NaN

    /**
     * The start position of the remainder
     */
    var remainderStartNegative: @DomainRelative @Negative Double = Double.NaN

    /**
     * The end of the negative remainder without adjacent gaps
     */
    var remainderEndNegative: @DomainRelative @Negative Double = Double.NaN

    val remainderNetSizePositive: @DomainRelative @Negative Double
      get() {
        return remainderEndPositive - remainderStartPositive
      }

    val remainderNetSizeNegative: @DomainRelative @Negative Double
      get() {
        return remainderEndNegative - remainderStartNegative
      }

    /**
     * How much has to be painted - in domain values.
     *
     * If no remainder is painted
     */
    var domainRangeToPaint: @Domain Double = 0.0

    /**
     * The paintable calculator
     */
    lateinit var calculator: ChartCalculator


    /**
     * The size of one gap - @ContentAreaRelative(!).
     *
     * Each segment is reduced in size depending on its size.
     * The gap between two segments is always the same.
     */
    var segmentGap: @ContentAreaRelative Double = 0.0

    /**
     * The end position (absolute) for each segment
     */
    val segmentEnd: DoubleArrayList = @DomainRelative DoubleArrayList(0)

    /**
     * The start position (absolute) for each segment
     */
    val segmentStart: DoubleArrayList = @DomainRelative DoubleArrayList(0)

    /**
     * Contains all domain relative values where no labels must be placed upon (e.g. the axis locations)
     */
    val valueLabelForbiddenLocations: DoubleArrayList = @DomainRelative DoubleArrayList(0)

    @DomainRelative
    var basePoint: Double = 0.0

    /**
     * Resets all values
     */
    fun reset() {
      basePoint = Double.NaN
      visibleSegmentsCountPositive = 0
      visibleSegmentsCountNegative = 0
      segmentsSumPositive = 0.0
      segmentsSumNegative = 0.0
      remainderPositiveVisible = false
      remainderNegativeVisible = false
      remainderEndPositive = Double.NaN
      remainderEndNegative = Double.NaN
      remainderStartPositive = Double.NaN
      remainderStartNegative = Double.NaN
      domainRangeToPaint = 0.0
      segmentGap = 0.0
      valueLabelForbiddenLocations.clear()
      segmentEnd.clear()
      segmentStart.clear()
    }

    /**
     * Calculates the layout. The layout information is stored in [calculateLayout]
     *
     *
     * General ideas:
     * - the gap is removed proportionally
     * - if a remainder is visible, one additional gap will be added. Therefore each segment is shown a little bit smaller.
     */
    fun calculateLayout(chartCalculator: ChartCalculator) {
      reset()
      this.calculator = chartCalculator

      val valueRange = data.valueRange

      //Calculate the sums (both negative and positive)
      var tmpSegmentsSumPositive = 0.0
      var tmpSegmentsSumNegative = 0.0
      data.valuesProvider.fastForEach { value: @Domain Double ->
        when {
          value > 0.0 -> {
            if (valueRange.end > 0.0) {
              tmpSegmentsSumPositive += value
              if (tmpSegmentsSumPositive > valueRange.start) {
                visibleSegmentsCountPositive++
              }
            }
          }

          value < 0.0 -> {
            if (valueRange.start < 0.0) {
              tmpSegmentsSumNegative += value
              if (tmpSegmentsSumNegative < valueRange.end) {
                visibleSegmentsCountNegative++
              }
            }
          }
        }
      }
      segmentsSumPositive = tmpSegmentsSumPositive - valueRange.start.coerceAtLeast(0.0)
      segmentsSumNegative = tmpSegmentsSumNegative - valueRange.end.coerceAtMost(0.0)


      //Add the remainder segment - if necessary
      if (style.showRemainderAsSegment) {
        //Necessary for positive?
        if (segmentsSumPositive < valueRange.end) {
          remainderPositiveVisible = true
          visibleSegmentsCountPositive++
        }
        //Necessary for negative?
        if (segmentsSumNegative > valueRange.start) {
          remainderNegativeVisible = true
          visibleSegmentsCountNegative++
        }
      }

      if (visibleSegments == 0) {
        //Do not paint anything if remainder is set to false and no segments are visible
        return
      }

      //Calculate the basics

      //
      // Gaps
      //
      //The general idea behind gaps is:
      //- each gap between two segments must have the same size
      //- segments must be reduced in size, to have space for the gaps
      //- (very) small segments should be reduced less than larger segments
      //
      // Therefore the segments are reduced proportional to their size
      //
      // Positive and negative gaps must be calculated independently - because the bars are aligned at 0.0 (!)

      //The number of gaps
      val gapCountPositive = visibleSegmentsCountPositive - 1
      val gapCountNegative = visibleSegmentsCountNegative - 1

      //Check if there is a gap at zero
      val gapAtZero = hasPositiveAndNegativeSegments
      @Zoomed val totalGapsSizeAtZero = if (gapAtZero) style.segmentsGap else 0.0

      //Calculate the total size of all gaps (summed up). This value is then distributed to each segment - depending on the size of each segment
      @Zoomed val totalGapsSizePositive = gapCountPositive * style.segmentsGap + totalGapsSizeAtZero / 2.0
      @Zoomed val totalGapsSizeNegative = gapCountNegative * style.segmentsGap + totalGapsSizeAtZero / 2.0

      //Convert the total gaps sum to content area relative
      @ContentAreaRelative val totalGapsSizeContentAreaRelativePositive: Double = when (style.orientation) {
        Orientation.Vertical -> calculator.zoomed2contentAreaRelativeY(totalGapsSizePositive)
        Orientation.Horizontal -> calculator.zoomed2contentAreaRelativeX(totalGapsSizePositive)
      }
      @ContentAreaRelative val totalGapsSizeContentAreaRelativeNegative: Double = when (style.orientation) {
        Orientation.Vertical -> calculator.zoomed2contentAreaRelativeY(totalGapsSizeNegative)
        Orientation.Horizontal -> calculator.zoomed2contentAreaRelativeX(totalGapsSizeNegative)
      }

      //The gap between two segments (the visible gap has the same size for all locations - independent of the size of the adjacent segments)
      segmentGap = when (style.orientation) {
        Orientation.Vertical -> calculator.zoomed2contentAreaRelativeY(style.segmentsGap)
        Orientation.Horizontal -> calculator.zoomed2contentAreaRelativeX(style.segmentsGap)
      }


      //Find the base point - @DomainRelative 0.0
      //The layout starts there - this is only relevant if the value range also contains a negative range
      basePoint = valueRange.toDomainRelative(0.0)

      //Layout the segments - adding half a gap to the start
      @DomainRelative @Positive1 var startYPositive: Double = basePoint
      @DomainRelative @Negative var startYNegative: Double = basePoint

      if (hasPositiveAndNegativeSegments) {
        //Introduce half a gap at zero
        startYPositive += segmentGap / 2.0
        startYNegative -= segmentGap / 2.0
      }

      data.valuesProvider.fastForEachIndexed { index, value: @MayBeNegative @Domain Double ->
        if (value == 0.0) {
          //Skip empty segments
          return@fastForEachIndexed
        }

        @DomainRelative @MayBeNegative val valueDomainRelative = valueRange.deltaToDomainRelative(value)

        //The gap reduction - is calculated domain relative
        @DomainRelative @MayBeNegative val gapReductionForSegment: Double = (if (value > 0) {
          totalGapsSizeContentAreaRelativePositive / valueRange.deltaPositive * value
        } else {
          totalGapsSizeContentAreaRelativeNegative / valueRange.deltaNegative * value
        }).or0ifNanOrInfinite() //work around division by 0

        //The net size for a segment
        @DomainRelative @MayBeNegative val segmentNetSize = valueDomainRelative - gapReductionForSegment

        //Calculate the *start*
        when {
          value > 0.0 -> {
            segmentStart[index] = startYPositive
            startYPositive += segmentGap
            startYPositive += segmentNetSize
          }

          value < 0.0 -> {
            segmentStart[index] = startYNegative
            startYNegative -= segmentGap
            startYNegative += segmentNetSize
          }

          else -> throw IllegalStateException("Invalid value: $value")
        }

        segmentEnd[index] = segmentStart[index] + segmentNetSize
      }

      //Assign the start positions for the remainders
      remainderStartPositive = if (remainderPositiveVisible) startYPositive else Double.NaN
      remainderEndPositive = if (remainderPositiveVisible) 1.0 else Double.NaN

      remainderStartNegative = if (remainderNegativeVisible) startYNegative else Double.NaN
      remainderEndNegative = if (remainderNegativeVisible) 0.0 else Double.NaN

      //Calculate the dead zones for the labels
      style.valueLabelForbiddenValuesProvider.fastForEach { forbiddenValue: @Domain Double ->
        @DomainRelative val forbiddenRelative = valueRange.toDomainRelative(forbiddenValue)
        valueLabelForbiddenLocations.add(forbiddenRelative)
      }
    }
  }

  /**
   * Contains the calculated values for the layout
   */
  internal val layout = Layout()

  override fun paint(paintingContext: LayerPaintingContext, x: @Window Double, y: @Window Double) {
    paintingContext.withCurrentChartState({ withContentAreaSize(Size(width, height)) }) {
      layout.calculateLayout(paintingContext.chartCalculator)

      if (layout.visibleSegments == 0) {
        //Nothing to show
        return@withCurrentChartState
      }

      val gc = paintingContext.gc
      gc.translate(x, y)
      gc.font(style.valueLabelFont)

      //Paint the background
      if (style.paintBackground) {
        gc.lineWidth = style.backgroundLineWidth
        gc.fill(style.backgroundColor)
        gc.stroke(style.backgroundBorderColor)

        val boundingBox = boundingBox(paintingContext)
        gc.fillRect(boundingBox)
        gc.strokeRect(boundingBox, StrokeLocation.Center)
      }

      //Delegate to the orientation dependent paint methods
      when (style.orientation) {
        Orientation.Vertical -> paintVertically(paintingContext)
        Orientation.Horizontal -> paintHorizontally(paintingContext)
      }
    }
  }

  /**
   * Paints horizontal bars
   */
  private fun paintHorizontally(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val calculator = layout.calculator
    val boundingBox = boundingBox(paintingContext)

    //Move domain relative 0.0 to the alignment point
    gc.translate(-calculator.domainRelative2zoomedX(0.0), 0.0)

    if (DebugFeature.ShowValues.enabled(paintingContext)) {
      calculator.domainRelative2zoomedX(0.0).let { x ->
        gc.paintMark(x, 0.0)
        gc.fillText("0.0", x, 0.0, Direction.CenterLeft)
      }
      calculator.domainRelative2zoomedX(1.0).let { x ->
        gc.paintMark(x, 0.0)
        gc.fillText("1.0", x, 0.0, Direction.CenterLeft)
      }
    }

    //
    //Paint all segments
    //

    @Zoomed val leftX = calculator.contentAreaRelative2zoomedX(1.0)
    @Zoomed val rightX = calculator.contentAreaRelative2zoomedX(0.0)

    //the label variables
    //The left value for the left most label (small @Zoomed values)
    @Zoomed var leftMostLabelLeft = leftX

    //The right value for the right most label (large @Zoomed values)
    @Zoomed var rightModeLabelRight = rightX

    @Zoomed var barMinX: Double? = null
    @Zoomed var barMaxX: Double? = null

    data.valuesProvider.fastForEachIndexed { index, value: @Domain Double ->
      if (value == 0.0) {
        //Skip empty segments
        return@fastForEachIndexed
      }

      @DomainRelative val segmentStart = layout.segmentStart[index]
      @DomainRelative val segmentEnd = layout.segmentEnd[index]

      @Zoomed val segmentStartX = calculator.domainRelative2zoomedX(segmentStart)
      @Zoomed val segmentEndX = calculator.domainRelative2zoomedX(segmentEnd)
      @Zoomed val segmentWidth = segmentEndX - segmentStartX

      if (barMinX == null) {
        barMinX = segmentStartX
      }
      if (barMaxX == null) {
        barMaxX = segmentStartX
      }
      barMinX = barMinX?.coerceAtMost(segmentStartX)?.coerceAtMost(segmentEndX)
      barMaxX = barMaxX?.coerceAtLeast(segmentStartX)?.coerceAtLeast(segmentEndX)

      val segmentColor = style.colorsProvider.valueAt(index)
      gc.fill(segmentColor)
      gc.stroke(segmentColor)
      gc.fillRoundedRect(segmentStartX, boundingBox.top, segmentWidth, boundingBox.getHeight(), style.segmentRadii)

      //Paint the label (if enabled)
      if (style.showValueLabels && (style.maxValueLabelWidth?.isPositive() == true)) {
        //The gap of the label
        @Zoomed val valueLabelGapVertical = height * 0.5 + style.valueLabelGapVertical
        @Zoomed val valueLabelGapHorizontal = height * 0.5 + style.valueLabelGapHorizontal

        //The value-label of the segment is placed at its horizontal center
        @Zoomed var textLocation = segmentStartX + segmentWidth / 2.0
        if (textLocation > leftX) {
          //The label is outside of the visible area; place it at the center of the visible part of the segment
          textLocation = if (segmentStartX < leftX) {
            segmentStartX + (leftX - segmentStartX) / 2.0
          } else {
            segmentEndX + (leftX - segmentEndX) / 2.0
          }
        }
        if (textLocation < rightX) {
          //The label is outside of the visible area; place it at the center of the visible part of the segment
          textLocation = if (segmentStartX > rightX) {
            segmentStartX - (segmentStartX - rightX) / 2.0
          } else {
            segmentEndX - (segmentEndX - rightX) / 2.0
          }
        }

        val labelText = style.valueLabelFormat.format(value)
        val labelTextWidth = gc.calculateTextWidth(labelText)

        @Zoomed val currentLabelLeft = textLocation + gc.calculateOffsetXWithAnchor(labelTextWidth, valueLabelGapHorizontal, style.valueLabelAnchorDirection.horizontalAlignment)
        @Zoomed val currentLabelRight = currentLabelLeft + labelTextWidth

        //Is the label forbidden? (e.g. overlaps an axis?)
        val forbidden = layout.valueLabelForbiddenLocations.fastFindAny {
          val forbiddenX = calculator.domainRelative2zoomedX(it)
          forbiddenX in currentLabelLeft..currentLabelRight
        }

        //check if the label does not overlap with existing labels
        if (
          !forbidden &&
          (currentLabelLeft > rightModeLabelRight //below the lowest
            || currentLabelRight < leftMostLabelLeft) //above the highest
        ) {
          gc.fill(style.valueLabelColor ?: segmentColor)
          gc.fillText(labelText, textLocation, 0.0, style.valueLabelAnchorDirection, valueLabelGapHorizontal, valueLabelGapVertical, style.maxValueLabelWidth, stringShortener = CanvasStringShortener.AllOrNothing)

          leftMostLabelLeft = min(leftMostLabelLeft, currentLabelLeft)
          rightModeLabelRight = max(rightModeLabelRight, currentLabelRight)
        } else {
          if (DebugFeature.OverlappingTexts.enabled(paintingContext)) {
            gc.fill(Color.orange)
            gc.fillText(labelText, textLocation, 0.0, style.valueLabelAnchorDirection, valueLabelGapHorizontal, valueLabelGapVertical, style.maxValueLabelWidth, stringShortener = CanvasStringShortener.AllOrNothing)
          }
        }
      }
    }

    if (style.showBorder) {
      val barMinimumX = barMinX
      val barMaximumX = barMaxX
      if (barMinimumX != null && barMaximumX != null) {
        gc.stroke(style.borderColor)
        gc.lineWidth = style.borderLineWidth
        if (barMinimumX < barMaximumX) {
          gc.strokeRoundedRect(barMinimumX, boundingBox.top, barMaximumX - barMinimumX, boundingBox.getHeight(), style.segmentRadii, StrokeLocation.Inside)
        }
      }
    }

    if (style.showRemainderAsSegment) {
      gc.stroke(style.remainderSegmentBorderColor)
      gc.lineWidth = style.remainderSegmentBorderLineWidth

      //Positive
      if (layout.remainderNetSizePositive > 0.0) {
        @Zoomed val startX = calculator.domainRelative2zoomedX(layout.remainderStartPositive)
        @Zoomed val endX = calculator.domainRelative2zoomedX(layout.remainderStartPositive + layout.remainderNetSizePositive)
        @Zoomed val width = endX - startX

        style.remainderSegmentBackgroundColor?.let {
          gc.fill(it)
          gc.fillRoundedRect(startX, boundingBox.top, width, boundingBox.getHeight(), style.segmentRadii)
        }

        gc.strokeRoundedRect(startX, boundingBox.top, width, boundingBox.getHeight(), style.segmentRadii, StrokeLocation.Inside)
      }

      //Negative
      if (layout.remainderNetSizeNegative < 0.0) {
        @Zoomed val startX = calculator.domainRelative2zoomedX(layout.remainderStartNegative)
        @Zoomed val endX = calculator.domainRelative2zoomedX(layout.remainderStartNegative + layout.remainderNetSizeNegative)
        @Zoomed val width = endX - startX

        style.remainderSegmentBackgroundColor?.let {
          gc.fill(it)
          gc.fillRoundedRect(startX, boundingBox.top, width, boundingBox.getHeight(), style.segmentRadii)
        }

        gc.strokeRoundedRect(startX, boundingBox.top, width, boundingBox.getHeight(), style.segmentRadii, StrokeLocation.Inside)
      }
    }
  }

  /**
   * Paints vertical bars
   */
  private fun paintVertically(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val calculator = layout.calculator
    val boundingBox = boundingBox(paintingContext)

    //Move domain relative 0.0 to the alignment point
    gc.translate(0.0, -calculator.domainRelative2zoomedY(0.0))

    if (DebugFeature.ShowValues.enabled(paintingContext)) {
      calculator.domainRelative2zoomedY(0.0).let { y ->
        gc.paintMark(0.0, y)
        gc.fillText("0.0", 0.0, y, Direction.CenterLeft)
      }
      calculator.domainRelative2zoomedY(1.0).let { y ->
        gc.paintMark(0.0, y)
        gc.fillText("1.0", 0.0, y, Direction.CenterLeft)
      }
    }

    //the height of a label
    @Zoomed val labelHeight = gc.getFontMetrics().totalHeight

    //
    //Paint all segments
    //

    @Zoomed val topY = calculator.contentAreaRelative2zoomedY(1.0)
    @Zoomed val bottomY = calculator.contentAreaRelative2zoomedY(0.0)

    //the label variables
    //The top value for the top most label (small @Zoomed values)
    @Zoomed var topmostLabelTop = topY

    //The lowest value for the under most label (large @Zoomed values)
    @Zoomed var undermostLabelBottom = bottomY

    @Zoomed var barMinY: Double? = null
    @Zoomed var barMaxY: Double? = null

    data.valuesProvider.fastForEachIndexed { index, value: @Domain Double ->
      if (value == 0.0) {
        //Skip empty segments
        return@fastForEachIndexed
      }

      @DomainRelative val segmentStart = layout.segmentStart[index]
      @DomainRelative val segmentEnd = layout.segmentEnd[index]

      @Zoomed val segmentStartY = calculator.domainRelative2zoomedY(segmentStart)
      @Zoomed val segmentEndY = calculator.domainRelative2zoomedY(segmentEnd)
      @Zoomed val segmentHeight = segmentEndY - segmentStartY

      if (barMinY == null) {
        barMinY = segmentStartY
      }
      if (barMaxY == null) {
        barMaxY = segmentStartY
      }
      barMinY = barMinY?.coerceAtMost(segmentStartY)?.coerceAtMost(segmentEndY)
      barMaxY = barMaxY?.coerceAtLeast(segmentStartY)?.coerceAtLeast(segmentEndY)

      val segmentColor = style.colorsProvider.valueAt(index)
      gc.fill(segmentColor)
      gc.stroke(segmentColor)
      gc.fillRoundedRect(boundingBox.left, segmentStartY, boundingBox.getWidth(), segmentHeight, style.segmentRadii)

      //Paint the label (if enabled)
      if (style.showValueLabels && (style.maxValueLabelWidth?.isPositive() == true)) {

        //The gap of the label
        @Zoomed val valueLabelGapHorizontal = width * 0.5 + style.valueLabelGapHorizontal
        @Zoomed val valueLabelGapVertical = height * 0.5 + style.valueLabelGapVertical

        //The value-label of the segment is placed at its vertical center
        @Zoomed var textLocation = segmentStartY + segmentHeight / 2.0
        if (textLocation > topY) {
          //The label is outside of the visible area; place it at the center of the visible part of the segment
          textLocation = if (segmentStartY < topY) {
            segmentStartY + (topY - segmentStartY) / 2.0
          } else {
            segmentEndY + (topY - segmentEndY) / 2.0
          }
        }
        if (textLocation < bottomY) {
          //The label is outside of the visible area; place it at the center of the visible part of the segment
          textLocation = if (segmentStartY > bottomY) {
            segmentStartY - (segmentStartY - bottomY) / 2.0
          } else {
            segmentEndY - (segmentEndY - bottomY) / 2.0
          }
        }

        @Zoomed val currentLabelTop = textLocation + gc.calculateOffsetYWithAnchor(labelHeight, valueLabelGapVertical, style.valueLabelAnchorDirection.verticalAlignment)
        @Zoomed val currentLabelBottom = currentLabelTop + labelHeight

        //Is the label forbidden? (e.g. overlaps an axis?)
        val forbidden = layout.valueLabelForbiddenLocations.fastFindAny {
          val forbiddenY = calculator.domainRelative2zoomedY(it)
          forbiddenY in currentLabelTop..currentLabelBottom
        }

        //check if the label does not overlap with existing labels
        if (
          !forbidden &&
          (currentLabelTop > undermostLabelBottom //below the lowest
            || currentLabelBottom < topmostLabelTop) //above the highest
        ) {
          gc.fill(style.valueLabelColor ?: segmentColor)
          gc.fillText(style.valueLabelFormat.format(value), 0.0, textLocation, style.valueLabelAnchorDirection, valueLabelGapHorizontal, valueLabelGapVertical, style.maxValueLabelWidth, stringShortener = CanvasStringShortener.AllOrNothing)

          topmostLabelTop = min(topmostLabelTop, currentLabelTop)
          undermostLabelBottom = max(undermostLabelBottom, currentLabelBottom)
        } else {
          if (DebugFeature.OverlappingTexts.enabled(paintingContext)) {
            gc.fill(Color.orange)
            gc.fillText(style.valueLabelFormat.format(value), 0.0, textLocation, style.valueLabelAnchorDirection, valueLabelGapHorizontal, valueLabelGapVertical, style.maxValueLabelWidth, stringShortener = CanvasStringShortener.AllOrNothing)
          }
        }
      }
    }

    if (style.showBorder) {
      val barMinimumY = barMinY
      val barMaximumY = barMaxY
      if (barMinimumY != null && barMaximumY != null) {
        gc.stroke(style.borderColor)
        gc.lineWidth = style.borderLineWidth
        if (barMinimumY < barMaximumY) {
          gc.strokeRoundedRect(boundingBox.left, barMinimumY, boundingBox.getWidth(), barMaximumY - barMinimumY, style.segmentRadii, StrokeLocation.Inside)
        }
      }
    }

    if (style.showRemainderAsSegment) {
      gc.stroke(style.remainderSegmentBorderColor)
      gc.lineWidth = style.remainderSegmentBorderLineWidth

      //Positive
      if (layout.remainderNetSizePositive > 0.0) {
        @Zoomed val startY = calculator.domainRelative2zoomedY(layout.remainderStartPositive)
        @Zoomed val endY = calculator.domainRelative2zoomedY(layout.remainderStartPositive + layout.remainderNetSizePositive)
        @Zoomed val height = endY - startY

        style.remainderSegmentBackgroundColor?.let {
          gc.fill(it)
          gc.fillRoundedRect(boundingBox.left, startY, boundingBox.getWidth(), height, style.segmentRadii)
        }

        gc.strokeRoundedRect(boundingBox.left, startY, boundingBox.getWidth(), height, style.segmentRadii, StrokeLocation.Inside)
      }

      //Negative
      if (layout.remainderNetSizeNegative < 0.0) {
        @Zoomed val startY = calculator.domainRelative2zoomedY(layout.remainderStartNegative)
        @Zoomed val endY = calculator.domainRelative2zoomedY(layout.remainderStartNegative + layout.remainderNetSizeNegative)
        @Zoomed val height = endY - startY

        style.remainderSegmentBackgroundColor?.let {
          gc.fill(it)
          gc.fillRoundedRect(boundingBox.left, startY, boundingBox.getWidth(), height, style.segmentRadii)
        }

        gc.strokeRoundedRect(boundingBox.left, startY, boundingBox.getWidth(), height, style.segmentRadii, StrokeLocation.Inside)
      }
    }
  }

  class Data(
    /**
     * Provides the values (Index: StackedBarValueIndex)
     */
    var valuesProvider: @Domain DoublesProvider = DefaultDoublesProvider(listOf(1.0, 5.0, 4.0)),
    /**
     * Stacked bars do only make sense with a linear value range
     */
    var valueRange: @Domain LinearValueRange = ValueRange.linear(0.0, 10.0),
  )

  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  @MultiProviderIndexContextAnnotation
  annotation class StackedBarValueIndex

  @ConfigurationDsl
  class Style {
    /**
     * The orientation of the bar.
     * The direction of the bar depends on the X/Y axis orientation.
     */
    var orientation: Orientation = Orientation.Vertical

    /**
     * Applies the given orientation - modifiers other properties to match the new orientation
     */
    fun applyOrientation(orientation: Orientation) {
      this.orientation = orientation

      valueLabelAnchorDirection = when (orientation) {
        Orientation.Vertical -> {
          Direction.CenterRight
        }

        Orientation.Horizontal -> {
          Direction.BottomCenter
        }
      }
    }

    /**
     * Provides the colors for the segments of a bar
     */
    var colorsProvider: MultiProvider<StackedBarValueIndex, Color> = MultiProvider.forListModulo(chartColors)

    /**
     * Format used for the labels of the values
     */
    var valueLabelFormat: CachedNumberFormat = decimalFormat

    /**
     * Font used for the labels of the values
     */
    var valueLabelFont: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * Color used for the labels of the values; set to null to use the same color as the corresponding segment
     */
    var valueLabelColor: Color? = null

    /**
     * Provides the domain values, where no label must be painted.
     * Returns the domain value - usually the position of the axis (e.g. 0.0)
     *
     * The returned values are used to calculate the *geometric* overlap of the values.
     * E.g.: If @Domain "0.0" is forbidden, a label close to 0.0 will be skipped, if it overlaps with that line!
     */
    val valueLabelForbiddenValuesProvider: @Domain DoublesProvider = DoublesProvider.forDoubles(0.0)

    /**
     * Whether to paint a border around a bar or not
     */
    var showBorder: Boolean = false

    /**
     * The color to paint the border around a bar with
     */
    var borderColor: Color = Color.darkgray

    /**
     * The width of the border around a bar
     */
    var borderLineWidth: @px Double = 1.0

    /**
     * Whether to paint a background or not
     */
    var paintBackground: Boolean = false

    /**
     * The color to paint the background
     */
    var backgroundColor: Color = Color.lightgray

    /**
     * The width of the background rect lines
     */
    var backgroundLineWidth: @px Double = 1.0

    /**
     * The color to paint the border of the background in
     */
    var backgroundBorderColor: Color = Color.web("#E6EAEC")

    /**
     * Radius for the top/bottom of the background rectangle
     */
    var backgroundRadius: @px Double = 5.0

    /**
     * Whether to paint any value labels at all
     */
    var showValueLabels: Boolean = true

    /**
     * Where to draw the value labels
     */
    var valueLabelAnchorDirection: Direction = Direction.CenterRight

    /**
     * Gap between bar and value labels
     */
    var valueLabelGapHorizontal: @px Double = 5.0
    var valueLabelGapVertical: @px Double = 5.0

    /**
     * The maximum width of the value label.
     * Can be set to avoid overlapping value labels with other bars.
     *
     * This var depends on at least:
     * * [valueLabelGap]
     * * [valueLabelAnchorDirection]
     * * width of the bar
     * * distance to the next bar
     */
    var maxValueLabelWidth: @px Double? = null

    /**
     * Whether to paint a segment that represents the difference of the value range and the sum of all segment values
     */
    var showRemainderAsSegment: Boolean = true

    /**
     * The background color of the segment that represents the difference of the value range and the sum of all segment values.
     */
    var remainderSegmentBackgroundColor: Color? = Color.white

    /**
     * The border color of the segment that represents the difference of the value range and the sum of all segment values.
     */
    var remainderSegmentBorderColor: Color = Theme.inactiveElementBorderColor()

    /**
     * The border line width of the segment that represents the difference of the value range and the sum of all segment values.
     */
    var remainderSegmentBorderLineWidth: @px Double = 2.0

    /**
     * Size of the segments gap
     */
    var segmentsGap: @Zoomed Double = 4.0

    /**
     * The radii of the corners of a segment
     */
    var segmentRadii: @px BorderRadius = BorderRadius.of(1.0)
  }
}
