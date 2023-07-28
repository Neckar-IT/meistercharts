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
package com.meistercharts.algorithms.layers

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.font.FontMetrics
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px

/**
 * Painting properties for "normal" axis that consists of:
 * * an axis line
 * * an optional title
 *
 * These properties/concepts are defined within:
 *
 * ## [axisStart] / [axisEnd] / [axisCenter] / [axisLength]
 * These values describe the coordinates, where the axis starts and ends: In the direction of the axis.
 * These values are mainly influenced by the content area / content are view port.
 *
 * * For a vertical axis (top to bottom) these values describe values on the y-axis
 * * For a horizontal axis (left to right) these values describe values on the x-axis
 *
 * ## [axisLineLocation]
 * Where the axis line is located.
 *
 */
interface AxisPaintingVariables : PaintingVariables {
  /**
   * Where the axis starts - in the direction of the line that is drawn. Used to calculate the length of the axis.
   */
  val axisStart: @Window Double

  /**
   * Where the axis ends - in the direction of the line that is drawn. Used to calculate the length of the axis.
   */
  val axisEnd: @Window Double

  /**
   * Returns the center of the axis
   */
  val axisCenter: @Window Double
    get() {
      return (axisStart + axisEnd) / 2.0
    }

  /**
   * The length of the axis line
   */
  val axisLength: @Zoomed Double
    get() {
      return axisEnd - axisStart
    }

  /**
   * The origin of the axis line. Depending on the side this might be the *center* or start of the axis line
   */
  val axisLineLocation: @Window Double

  /**
   * The max width that is available for the tick value labels
   */
  val tickValueLabelMaxWidth: @px @MayBeNaN Double


  /**
   * The location of the axis title.
   * The title should be aligned at the given location.
   *
   * This location places the axis *parallel* to the axis line.
   * The title is usually centered between [axisStart] and [axisEnd].
   */
  val axisTitleLocation: @Window Double

  /**
   * The origin for the "content" of the axis (excluding title).
   * Depending on the side, the axis is painted to the left or right, bottom or top.
   *
   * The content is painted towards the *inside* of the location.
   */
  val axisContentLocation: @Window Double

  /**
   * The width/height (depending on the orientation) that has been used for the title including the title-gap.
   * Does *not* include the margin.
   *
   *
   * Other explanation:
   * How much space is used for the title.
   * This value contains 0.0 if the title is not visible.
   *
   * This is the space for the title label that is parallel to the axis.
   * e.g. for [Side.Left] the title is rotated by 90°.
   * The height of the title is then the *x* value
   */
  val spaceForTitleIncludingGap: @px Double
}


abstract class AxisPaintingVariablesImpl : AxisPaintingVariables {
  /**
   * The font metrics for the axis title
   */
  var titleFontMetrics: FontMetrics = FontMetrics.empty

  /**
   * The font metrics for the ticks
   */
  var tickFontMetrics: FontMetrics = FontMetrics.empty

  /**
   * The *outer* side of the axis (including title)
   * Depending on the side, the axis is painted to the left or right
   */
  override var axisTitleLocation: @Window Double = 0.0

  /**
   * The *outer* side of the axis (excluding title).
   * Depending on the side, the axis is painted to the left or right
   */
  override var axisContentLocation: @Window Double = 0.0

  /**
   * The start of the axis
   */
  override var axisStart: @Window Double = 0.0

  /**
   * Where the axis ends
   */
  override var axisEnd: @Window Double = 0.0


  /**
   * How much space is used for the title.
   * This value contains 0.0 if the title is not visible.
   *
   * This is the space for the title label that is parallel to the axis.
   * e.g. for [Side.Left] the title is rotated by 90°.
   * The height of the title is then the *x* value
   */
  override var spaceForTitleIncludingGap: @px Double = 0.0

  /**
   * The location of the axis line. This value describes the location where the axis intersects the corresponding axis.
   * Depending on the side this is either interpreted as x or y value
   */
  override var axisLineLocation: @Window Double = 0.0

  /**
   * The max width that is available for the tick value labels
   */
  override var tickValueLabelMaxWidth: @px Double = 0.0


  /**
   * Resets all variables to their default values
   */
  open fun reset() {
    axisTitleLocation = Double.NaN
    axisTitleLocation = Double.NaN
    axisContentLocation = Double.NaN
    axisContentLocation = Double.NaN
    axisLineLocation = Double.NaN

    axisStart = Double.NaN
    axisEnd = Double.NaN
    spaceForTitleIncludingGap = Double.NaN

    tickValueLabelMaxWidth = Double.NaN


    tickFontMetrics = FontMetrics.empty
    titleFontMetrics = FontMetrics.empty
  }

  /**
   * Updates the tick font metrics
   */
  fun calculateTickFontMetrics(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
  ) {
    paintingContext.gc.font(style.tickFont)
    tickFontMetrics = paintingContext.gc.getFontMetrics()
  }

  /**
   * Calculates the title related stuff
   */
  fun calculateTitle(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
  ) {
    //TODO check orientation!
    //Calculate the space for the title
    spaceForTitleIncludingGap = if (style.titleVisible() && style.hasNonBlankTitle(paintingContext.chartSupport)) {
      //Title font related calculations
      paintingContext.gc.font(style.titleFont)
      titleFontMetrics = paintingContext.gc.getFontMetrics()

      titleFontMetrics.totalHeight + style.titleGap
    } else {
      0.0
    }
  }

  /**
   * Calculates axis start and end
   */
  fun calculateAxisStartEnd(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
  ) {
    //Calculate start/end of the axis
    val chartCalculator = paintingContext.chartCalculator
    //@Zoomed val axisPasspartout = style.axisPasspartout()

    when (style.orientation) {
      // |
      // |
      // |
      Orientation.Vertical -> {
        when (style.paintRange) {
          AxisStyle.PaintRange.ContentArea -> {
            axisStart = chartCalculator.contentAreaRelative2windowYInViewport(0.0)
            axisEnd = chartCalculator.contentAreaRelative2windowYInViewport(1.0)
          }

          AxisStyle.PaintRange.Continuous -> {
            axisStart = chartCalculator.contentViewportMinY()
            axisEnd = chartCalculator.contentViewportMaxY()
          }
        }
      }

      // ------
      Orientation.Horizontal ->
        when (style.paintRange) {
          AxisStyle.PaintRange.ContentArea -> {
            axisStart = chartCalculator.contentAreaRelative2windowXInViewport(0.0)
            axisEnd = chartCalculator.contentAreaRelative2windowXInViewport(1.0)
          }

          AxisStyle.PaintRange.Continuous -> {
            axisStart = chartCalculator.contentViewportMinX()
            axisEnd = chartCalculator.contentViewportMaxX()
          }
        }
    }
  }

  /**
   * Calculates the max widths for the tick labels.
   *
   * For vertical axis:
   * The remaining space for the axis after subtracting:
   * - title
   * - axis line width
   * - tick size + gap
   *
   * For horizontal axis:
   * - currently unlimited
   */
  fun calculateTickLabelsMaxWidth(
    style: AxisStyle,
  ) {
    //Calculate the max width for the tick value label
    tickValueLabelMaxWidth = when (style.orientation) {
      Orientation.Vertical -> calculateTickLabelsMaxWidthVertical(style)

      Orientation.Horizontal -> {
        calculateTickLabelsMaxWidthHorizontal()
      }
    }
  }

  /**
   * Calculates the tick label width for horizontal axis
   */
  protected open fun calculateTickLabelsMaxWidthHorizontal(): @Zoomed Double {
    return Double.NaN
  }

  /**
   * Calculates the tick label width for vertical axis
   */
  protected open fun calculateTickLabelsMaxWidthVertical(style: AxisStyle): @Zoomed Double {
    return (style.size
      - style.axisLineWidth
      - style.tickLength
      - style.tickLabelGap
      - spaceForTitleIncludingGap)
  }

  /**
   * Calculates the locations
   */
  fun calculateLocations(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
  ) {
    when (style.side) {
      Side.Left -> {
        axisTitleLocation = style.margin.left
        axisContentLocation = axisTitleLocation + spaceForTitleIncludingGap

        when (style.tickOrientation) {
          Vicinity.Inside -> {
            axisLineLocation = axisContentLocation + style.axisLineWidth / 2.0
          }

          Vicinity.Outside -> {
            axisLineLocation = axisTitleLocation + style.size - style.axisLineWidth / 2.0
          }
        }
      }

      Side.Right -> {
        axisTitleLocation = paintingContext.width - style.margin.right
        axisContentLocation = axisTitleLocation - spaceForTitleIncludingGap

        when (style.tickOrientation) {
          Vicinity.Inside -> {
            axisLineLocation = axisContentLocation - style.axisLineWidth / 2.0
          }

          Vicinity.Outside -> {
            axisLineLocation = axisTitleLocation - style.size + style.axisLineWidth / 2.0
          }
        }
      }

      Side.Top -> {
        axisTitleLocation = style.margin.top
        axisContentLocation = axisTitleLocation + spaceForTitleIncludingGap

        when (style.tickOrientation) {
          Vicinity.Inside -> {
            axisLineLocation = axisContentLocation + style.axisLineWidth / 2.0
          }

          Vicinity.Outside -> {
            axisLineLocation = axisTitleLocation + style.size - style.axisLineWidth / 2.0
          }
        }
      }

      Side.Bottom -> {
        axisTitleLocation = paintingContext.height - style.margin.bottom
        axisContentLocation = axisTitleLocation - spaceForTitleIncludingGap

        when (style.tickOrientation) {
          Vicinity.Inside -> {
            axisLineLocation = axisContentLocation - style.axisLineWidth / 2.0
          }

          Vicinity.Outside -> {
            axisLineLocation = axisTitleLocation - style.size + style.axisLineWidth / 2.0
          }
        }
      }
    }

  }

}
