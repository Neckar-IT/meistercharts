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
package com.meistercharts.canvas

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.canvas.geometry.BezierCurve
import com.meistercharts.canvas.geometry.BezierCurveRect
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.canvas.text.TextLineCalculations
import com.meistercharts.color.Color
import com.meistercharts.color.get
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.model.*
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.kotlin.lang.ifBlank
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.kotlin.lang.isPositiveOrZero
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.unit.other.px
import kotlin.math.max

/**
 * Fills a rectangle using an anchor direction
 */
fun CanvasRenderingContext.fillRect(
  x: @px Double,
  y: @px Double,
  width: @MayBeNegative @px @Zoomed Double,
  height: @MayBeNegative @px @Zoomed Double,
  /**
   * Describes the anchor direction as viewed from the rectangle
   *
   * Examples:
   * * if the anchor direction is set to [Direction.TopLeft], the rectangle is painted to the right and below of x/y
   * * if the anchor direction is set to [Direction.TopRight], the rectangle is painted to the left and below of x/y
   * * if the anchor direction is set to [Direction.Center], the center of the rectangle is placed directly on x/y
   */
  anchorDirection: Direction,
  /**
   * The gap that is applied to the anchor
   */
  anchorGapHorizontal: @px Double = 0.0,
  anchorGapVertical: @px Double = anchorGapHorizontal,
) {

  val relevantX = x + calculateOffsetXWithAnchor(width, anchorGapHorizontal, anchorDirection.horizontalAlignment)
  val relevantY = y + calculateOffsetYWithAnchor(height, anchorGapVertical, anchorDirection.verticalAlignment)

  fillRect(relevantX, relevantY, width, height)
}

fun CanvasRenderingContext.strokeRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,
  /**
   * Describes the anchor direction as viewed from the rectangle
   *
   * Examples:
   * * if the anchor direction is set to [Direction.TopLeft], the rectangle is painted to the right and below of x/y
   * * if the anchor direction is set to [Direction.TopRight], the rectangle is painted to the left and below of x/y
   * * if the anchor direction is set to [Direction.Center], the center of the rectangle is placed directly on x/y
   */
  anchorDirection: Direction,
  /**
   * The gap that is applied to the anchor
   */
  anchorGapHorizontal: @px Double = 0.0,
  anchorGapVertical: @px Double = anchorGapHorizontal,

  strokeLocation: StrokeLocation = StrokeLocation.Center,
) {

  val relevantX = x + calculateOffsetXWithAnchor(width, anchorGapHorizontal, anchorDirection.horizontalAlignment)
  val relevantY = y + calculateOffsetYWithAnchor(height, anchorGapVertical, anchorDirection.verticalAlignment)

  strokeRect(relevantX, relevantY, width, height, strokeLocation)
}

/**
 * Calculates the necessary translation when painting a rectangle
 */
fun CanvasRenderingContext.calculateOffsetYWithAnchor(
  height: @px Double,

  anchorGapVertical: @px Double = 0.0,

  verticalAlignment: VerticalAlignment,
): @px Double {
  return verticalAlignment.calculateOffsetYWithAnchor(height, anchorGapVertical, this)
}

fun CanvasRenderingContext.findYWithAnchor(
  height: @px Double,

  anchorGapVertical: @px Double = 0.0,

  verticalAlignment: VerticalAlignment,
): @px Double {
  return verticalAlignment.findYWithAnchor(height, anchorGapVertical, this)
}

fun HorizontalAlignment.calculateOffsetXWithAnchor(
  /**
   * The width
   */
  width: @px Double,
  /**
   * The gap
   */
  anchorGapHorizontal: @px Double,
): @px Double {
  return when (this) {
    HorizontalAlignment.Left -> 0.0
    HorizontalAlignment.Center -> -width / 2.0
    HorizontalAlignment.Right -> -width
  } + this.calculateOffsetXForGap(anchorGapHorizontal)
}

/**
 * Returns the y offset when painting a box with an anchor - assuming the box is painted with the
 * given height to the bottom.
 *
 * This method can be used when a rectangle or box is painted with an anchor.
 *
 * The returned value is relative to the upper left corner of the assumed rectangle
 *
 * This method will throw an exception, if this is [VerticalAlignment.Baseline] and no rendering context is provided
 */
fun VerticalAlignment.calculateOffsetYWithAnchor(
  /**
   * The height
   */
  height: @px Double,
  /**
   * The anchor gap
   */
  anchorGapVertical: @px Double,
  /**
   * The rendering context. If no rendering context is provided, [VerticalAlignment.Baseline] returns the same as [VerticalAlignment.Center]
   */
  gc: CanvasRenderingContext?,
): @px Double {
  return when (this) {
    VerticalAlignment.Top -> 0.0
    VerticalAlignment.Center -> -height / 2.0
    VerticalAlignment.Baseline -> {
      if (gc == null) {
        -height / 2.0
      } else {
        //y is currently the location of the base line
        val fontMetrics = gc.getFontMetrics()
        -height / 2.0 - fontMetrics.accentLine / 2.0 + fontMetrics.pLine / 2.0 //Fit the center of the line to the center of the box
      }
    }

    VerticalAlignment.Bottom -> -height
  } + calculateOffsetYForGap(anchorGapVertical)
}

fun VerticalAlignment.findYWithAnchor(
  /**
   * The height
   */
  height: @px Double,
  /**
   * The anchor gap
   */
  anchorGapVertical: @px Double = 0.0,
  /**
   * The rendering context. If no rendering context is provided, [VerticalAlignment.Baseline] returns the same as [VerticalAlignment.Center]
   */
  gc: CanvasRenderingContext? = null,
): @px Double {
  return when (this) {
    VerticalAlignment.Top -> 0.0
    VerticalAlignment.Center -> height / 2.0
    VerticalAlignment.Baseline -> {
      if (gc == null) {
        height / 2.0
      } else {
        //y is currently the location of the baseline
        val fontMetrics = gc.getFontMetrics()
        height / 2.0 + fontMetrics.accentLine / 2.0 - fontMetrics.pLine / 2.0 //Fit the center of the line to the center of the box
      }
    }

    VerticalAlignment.Bottom -> height
  } + calculateOffsetYForGap(anchorGapVertical)
}

/**
 * Returns the x value within a provided width
 */
fun HorizontalAlignment.findXWithAnchor(
  width: @px Double,
  /**
   * The anchor gap
   */
  anchorGapHorizontal: @px Double = 0.0,
): Double {
  return when (this) {
    HorizontalAlignment.Left -> 0.0
    HorizontalAlignment.Center -> width / 2.0
    HorizontalAlignment.Right -> width
  } + calculateOffsetXForGap(anchorGapHorizontal)
}

/**
 * Returns the x offset when painting a box with an anchor - assuming the box is painted with the
 * given width to the right.
 *
 * This method can be used when a rectangle or box is painted with an anchor.
 *
 * The returned value is relative to the upper left corner of the assumed rectangle
 * Returns negative values for [HorizontalAlignment.Right].
 *
 * ATTENTION: When placing something *within* a rectangle, use [findXWithAnchor]
 */
@Suppress("unused", "UnusedReceiverParameter") //Extension method to keep it symmetric to the Y-Version of this method
fun CanvasRenderingContext.calculateOffsetXWithAnchor(
  /**
   * The width
   */
  width: @px Double,
  /**
   * The gap
   */
  anchorGapHorizontal: @px Double,
  /**
   * The alignment
   */
  horizontalAlignment: HorizontalAlignment,
): @px Double {
  return horizontalAlignment.calculateOffsetXWithAnchor(width, anchorGapHorizontal)
}

/**
 * Find the x location within the provided width.
 *
 * Returns positive values for [HorizontalAlignment.Right]
 */
@Suppress("unused", "UnusedReceiverParameter") //Extension method to keep it symmetric to the Y-Version of this method
fun CanvasRenderingContext.findXWithAnchor(
  /**
   * The width
   */
  width: @px Double,
  /**
   * The gap
   */
  anchorGapHorizontal: @px Double,
  /**
   * The alignment
   */
  horizontalAlignment: HorizontalAlignment,
): @px Double {
  return horizontalAlignment.findXWithAnchor(width) + horizontalAlignment.calculateOffsetXForGap(anchorGapHorizontal)
}

/**
 * Strokes a rectangle with rounded corners
 */
fun CanvasRenderingContext.strokeRoundedRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,
  /**
   * The radius for all corners
   */
  @Zoomed @px radius: Double,
  strokeLocation: StrokeLocation = StrokeLocation.Center,
) {
  strokeRoundedRect(x, y, width, height, radius, radius, radius, radius, strokeLocation)
}

/**
 * Stroke a rounded rect with the given radii
 */
fun CanvasRenderingContext.strokeRoundedRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,

  @Zoomed @px radiusTopLeft: Double,
  @Zoomed @px radiusTopRight: Double,
  @Zoomed @px radiusBottomRight: Double,
  @Zoomed @px radiusBottomLeft: Double,
  strokeLocation: StrokeLocation = StrokeLocation.Center,
) {
  roundedRect(x, y, width, height, radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft, strokeLocation)
  stroke()
}

fun CanvasRenderingContext.strokeRoundedRect(@Window @px bounds: Rectangle, @Zoomed @px radius: Double, strokeLocation: StrokeLocation = StrokeLocation.Center) {
  strokeRoundedRect(bounds.left, bounds.top, bounds.widthAbs, bounds.heightAbs, radius, strokeLocation)
}

fun CanvasRenderingContext.strokeRoundedRect(@Window @px bounds: Rectangle, @Zoomed @px radii: BorderRadius?, strokeLocation: StrokeLocation = StrokeLocation.Center) {
  strokeRoundedRect(bounds.left, bounds.top, bounds.widthAbs, bounds.heightAbs, radii, strokeLocation)
}

fun CanvasRenderingContext.strokeRoundedRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,

  @Zoomed @px radii: BorderRadius?,
  /**
   * The stroke location of the line
   */
  strokeLocation: StrokeLocation = StrokeLocation.Center,
) {
  if (radii == null || radii.isEmpty) {
    strokeRect(x, y, width, height, strokeLocation)
  } else {
    roundedRect(x, y, width, height, radii.topLeft, radii.topRight, radii.bottomRight, radii.bottomLeft, strokeLocation)
    stroke()
  }
}

fun CanvasRenderingContext.fillRoundedRect(@Window @px bounds: Rectangle, @Zoomed @px radius: Double) {
  fillRoundedRect(bounds.left, bounds.top, bounds.widthAbs, bounds.heightAbs, radius)
}

/**
 * Fills a rectangle with rounded corners
 */
fun CanvasRenderingContext.fillRoundedRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,
  /**
   * The radius for all corners
   */
  @Zoomed @px radius: Double,
) {
  fillRoundedRect(x, y, width, height, radius, radius, radius, radius)
}

/**
 * Fill a rounded rect with the given radii
 */
fun CanvasRenderingContext.fillRoundedRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,

  @Zoomed @px radiusTopLeft: Double,
  @Zoomed @px radiusTopRight: Double,
  @Zoomed @px radiusBottomRight: Double,
  @Zoomed @px radiusBottomLeft: Double,
) {
  if (radiusTopLeft == 0.0 &&
    radiusTopRight == 0.0 &&
    radiusBottomRight == 0.0 &&
    radiusBottomLeft == 0.0
  ) {
    fillRect(x, y, width, height)
  } else {
    roundedRect(x, y, width, height, radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft)
    fill()
  }
}

fun CanvasRenderingContext.fillRoundedRect(@Window @px bounds: Rectangle, @Zoomed @px radii: BorderRadius?) {
  fillRoundedRect(bounds.left, bounds.top, bounds.widthAbs, bounds.heightAbs, radii)
}

fun CanvasRenderingContext.fillRoundedRect(
  @px x: Double,
  @px y: Double,
  @MayBeNegative @px @Zoomed width: Double,
  @MayBeNegative @px @Zoomed height: Double,

  @Zoomed @px radii: BorderRadius?,
) {
  if (radii == null || radii.isEmpty) {
    fillRect(x, y, width, height)
  } else {
    roundedRect(x, y, width, height, radii.topLeft, radii.topRight, radii.bottomRight, radii.bottomLeft)
    fill()
  }
}

/**
 * Appends a new path containing a rounded rect
 */
fun CanvasRenderingContext.roundedRect(x: Double, y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double, @Zoomed radius: Double, strokeLocation: StrokeLocation = StrokeLocation.Center) {
  roundedRect(x, y, width, height, radius, radius, radius, radius, strokeLocation)
}

/**
 * Appends a new path containing a rounded rect
 */
fun CanvasRenderingContext.roundedRect(x: Double, y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double, @Zoomed @px radii: BorderRadius, strokeLocation: StrokeLocation = StrokeLocation.Center) {
  roundedRect(x, y, width, height, radii.topLeft, radii.topRight, radii.bottomRight, radii.bottomLeft, strokeLocation)
}

/**
 * Paints a rounded rect
 */
fun CanvasRenderingContext.roundedRect(
  x: Double,
  y: Double,
  @MayBeNegative width: Double,
  @MayBeNegative height: Double,

  radiusTopLeft: Double,
  radiusTopRight: Double,
  radiusBottomRight: Double,
  radiusBottomLeft: Double,

  /**
   * The stroke location.
   * ATTENTION: If the stroke location is set to another value, the *current* line width of the gc is used
   */
  strokeLocation: StrokeLocation = StrokeLocation.Center,
) {
  var calculatedWidth: Double
  var calculatedX: Double

  if (width >= 0.0) {
    calculatedWidth = width
    calculatedX = x
  } else {
    calculatedWidth = -width
    calculatedX = x - calculatedWidth
  }

  var calculatedHeight: Double
  var calculatedY: Double

  if (height < 0.0) {
    calculatedHeight = -height
    calculatedY = y - calculatedHeight
  } else {
    calculatedHeight = height
    calculatedY = y
  }

  when (strokeLocation) {
    StrokeLocation.Center -> {
      //Nothing to change
    }

    StrokeLocation.Inside -> {
      val currentLineWidth = lineWidth

      calculatedX += currentLineWidth / 2.0
      calculatedY += currentLineWidth / 2.0

      calculatedWidth -= currentLineWidth
      calculatedHeight -= currentLineWidth
    }

    StrokeLocation.Outside -> {
      val currentLineWidth = lineWidth

      calculatedX -= currentLineWidth / 2.0
      calculatedY -= currentLineWidth / 2.0

      calculatedWidth += currentLineWidth
      calculatedHeight += currentLineWidth
    }
  }

  roundedRectInternal(calculatedX, calculatedY, calculatedWidth, calculatedHeight, radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft)
}

/**
 * Creates a path with *non negative* width/heights.
 *
 * ATTENTION:
 * * If the width or height are negative, a "normal" rectangle is added instead
 * * if the radii ar larger than width/height, a "normal" rectangle is added instead
 *
 */
private fun CanvasRenderingContext.roundedRectInternal(
  x: Double,
  y: Double,
  width: @MayBeNegative Double,
  height: @MayBeNegative Double,
  radiusTopLeft: @PositiveOrZero Double,
  radiusTopRight: @PositiveOrZero Double,
  radiusBottomRight: @PositiveOrZero Double,
  radiusBottomLeft: @PositiveOrZero Double,
) {
  //Check if it possible to paint this as rounded rect
  if (
    width < 0.0 || height < 0.0 || //we have negative width/height - therefore it is not possible to paint it
    width < radiusTopLeft + radiusTopRight || //too small compared to the radii at the top
    width < radiusBottomLeft + radiusBottomRight || //too small compared to the radii at the bottom
    height < radiusTopLeft + radiusBottomLeft || //too small compared to the radii at the left side
    height < radiusTopRight + radiusBottomRight //too small compared to the radii at the right side
  ) {
    //too small, draw a regular rect
    beginPath()
    rect(x, y, width, height)
    closePath()

    return
  }

  require(width.isPositive()) { "width must be > 0 but was <$width>" }
  require(height.isPositive()) { "height must be > 0 but was <$height>" }

  require(radiusTopLeft.isPositiveOrZero()) { "radiusTopLeft must be >= 0 but was <$radiusTopLeft>" }
  require(radiusTopRight.isPositiveOrZero()) { "radiusTopRight must be >= 0 but was <$radiusTopRight>" }
  require(radiusBottomRight.isPositiveOrZero()) { "radiusBottomRight must be >= 0 but was <$radiusBottomRight>" }
  require(radiusBottomLeft.isPositiveOrZero()) { "radiusBottomLeft must be >= 0 but was <$radiusBottomLeft>" }

  @Zoomed @px val fixedRadiusTopLeft: Double = radiusTopLeft.coerceAtMost(width / 2.0).coerceAtMost(height / 2.0)
  @Zoomed @px val fixedRadiusTopRight: Double = radiusTopRight.coerceAtMost(width / 2.0).coerceAtMost(height / 2.0)
  @Zoomed @px val fixedRadiusBottomRight: Double = radiusBottomRight.coerceAtMost(width / 2.0).coerceAtMost(height / 2.0)
  @Zoomed @px val fixedRadiusBottomLeft: Double = radiusBottomLeft.coerceAtMost(width / 2.0).coerceAtMost(height / 2.0)

  beginPath()

  //top left corner
  moveTo(x, y + fixedRadiusTopLeft)
  arcTo(x, y, x + fixedRadiusTopLeft, y, radiusTopRight)

  //top right corner
  arcTo(x + width, y, x + width, y + fixedRadiusTopRight, fixedRadiusTopRight)

  //bottom right corner
  lineTo(x + width, y + height - fixedRadiusBottomRight)
  arcTo(x + width, y + height, x + width - fixedRadiusBottomRight, y + height, fixedRadiusBottomRight)

  //bottom left corner
  lineTo(x + fixedRadiusBottomLeft, y + height)
  arcTo(x, y + height, x, y + height - fixedRadiusBottomLeft, fixedRadiusBottomLeft)

  closePath()
}

/**
 * Translates the canvas to the content area origin (top left)
 */
fun CanvasRenderingContext.translateToContentAreaOrigin(chartCalculator: ChartCalculator) {
  translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))
}


/**
 * Strokes a cross at [Coordinates] using [baseWidth] to calculate fatness
 */
fun CanvasRenderingContext.strokeCross(@Window x: Double = 0.0, @Window y: Double = 0.0, @Zoomed size: Double = 5.0) {
  val halfSize = size / 2.0
  strokeLine(x - halfSize, y, x + halfSize, y)
  strokeLine(x, y - halfSize, x, y + halfSize)
}

fun CanvasRenderingContext.strokeCross(@Window location: Coordinates, @Zoomed size: Double = 5.0) {
  strokeCross(location.x, location.y, size)
}

/**
 * Strokes a cross at 45° angle at [Coordinates] using [baseWidth] to calculate fatness
 */
fun CanvasRenderingContext.strokeCross45Degrees(@Window x: Double = 0.0, @Window y: Double = 0.0, @Zoomed size: Double = 5.0) {
  val halfSize = size / 2.0
  strokeLine(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
  strokeLine(x - halfSize, y + halfSize, x + halfSize, y - halfSize)
}

/**
 * Helper methods that marks the current location (translation of the rendering context).
 * Should only be used for debugging
 */
fun CanvasRenderingContext.paintLocation(location: Coordinates, color: Color = Color.orangered()) {
  paintLocation(location.x, location.y, color)
}

/**
 * Helper methods that marks the current location (translation of the rendering context).
 * Should only be used for debugging
 */
fun CanvasRenderingContext.paintLocation(x: Double = 0.0, y: Double = 0.0, color: Color = Color.orangered(), label: String? = null) {
  //Debug function - should not modify GC
  saved {
    lineWidth = 1.0
    translate(x, y)
    stroke(color)
    strokeLine(-1000.0, 0.0, 1000.0, 0.0) //horizontal
    strokeLine(0.0, 1000.0, 0.0, -1000.0) //vertical

    strokeLine(-1000.0, -1000.0, 1000.0, 1000.0) //diagonal
    strokeLine(-1000.0, 1000.0, 1000.0, -1000.0) //diagonal

    strokeOvalCenter(0.0, 0.0, 15.0, 15.0)

    label?.let {
      stroke(Color.white)
      fill(color)
      strokeText(text = label, x = 0.0, y = 0.0, anchorDirection = Direction.CenterLeft, gapHorizontal = 10.0)
      fillText(text = label, x = 0.0, y = 0.0, anchorDirection = Direction.CenterLeft, gapHorizontal = 10.0)
    }
  }
}

/**
 * Paints a mark (circle with cross) at the given location
 *
 */
fun CanvasRenderingContext.paintMark(location: Coordinates = Coordinates.origin, radius: Double = 5.0, color: Color? = null) {
  paintMark(location.x, location.y, radius, color)
}

/**
 * Paints a mark (circle with cross) at the given location
 */
fun CanvasRenderingContext.paintMark(x: Double = 0.0, y: Double = 0.0, radius: Double = 5.0, color: Color? = null, label: String? = null) {
  //Debug function - should not modify GC
  saved {
    color?.let {
      stroke(color)
    }

    strokeLine(x - radius, y, x + radius, y) //horizontal
    strokeLine(x, y - radius, x, y + radius) //vertical

    strokeOvalCenter(x, y, radius * 2, radius * 2)

    label?.let {
      stroke(Color.white)
      fill(color ?: Color.black())
      strokeText(text = label, x = x, y = y, anchorDirection = Direction.CenterLeft, gapHorizontal = 10.0)
      fillText(text = label, x = x, y = y, anchorDirection = Direction.CenterLeft, gapHorizontal = 10.0)
    }
  }
}

/**
 * Strokes a rect using coordinates for both sides
 */
fun CanvasRenderingContext.strokeRectCoordinates(x0: Double, y0: Double, x1: Double, y1: Double, strokeLocation: StrokeLocation = StrokeLocation.Center) {
  strokeRect(x0, y0, x1 - x0, y1 - y0, strokeLocation)
}

fun CanvasRenderingContext.strokeRectCoordinates(corner0: Coordinates, corner1: Coordinates, strokeLocation: StrokeLocation = StrokeLocation.Center) {
  strokeRectCoordinates(corner0.x, corner0.y, corner1.x, corner1.y, strokeLocation)
}

/**
 * Fills a rect using coordinates for both sides
 */
fun CanvasRenderingContext.fillRectCoordinates(x0: Double, y0: Double, x1: Double, y1: Double) {
  fillRect(x0, y0, x1 - x0, y1 - y0)
}

fun CanvasRenderingContext.fillRectCoordinates(corner0: Coordinates, corner1: Coordinates) {
  fillRectCoordinates(corner0.x, corner0.y, corner1.x, corner1.y)
}

fun CanvasRenderingContext.fillRoundedRectCoordinates(
  x0: Double, y0: Double, x1: Double, y1: Double,
  @Zoomed @px radiusTopLeft: Double,
  @Zoomed @px radiusTopRight: Double = radiusTopLeft,
  @Zoomed @px radiusBottomRight: Double = radiusTopLeft,
  @Zoomed @px radiusBottomLeft: Double = radiusTopLeft,
) {
  fillRoundedRect(x0, y0, x1 - x0, y1 - y0, radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft)
}

fun CanvasRenderingContext.strokeRoundedRectCoordinates(
  x0: Double, y0: Double, x1: Double, y1: Double,
  @Zoomed @px radiusTopLeft: Double,
  @Zoomed @px radiusTopRight: Double = radiusTopLeft,
  @Zoomed @px radiusBottomRight: Double = radiusTopLeft,
  @Zoomed @px radiusBottomLeft: Double = radiusTopLeft,
) {
  strokeRoundedRect(x0, y0, x1 - x0, y1 - y0, radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft)
}

/**
 * Helper method that helps detecting the method - takes the layer painting context as parameter
 */
fun CanvasRenderingContext.paintTextWithPaintable(
  text: String,
  paintable: Paintable,
  paintingContext: LayerPaintingContext,
  paintableLocation: PaintableLocation = PaintableLocation.PaintableInside,
  anchorDirection: Direction,
  /**
   * The gap between text and paintable
   */
  @px gap: Double = 5.0,
) {
  paintingContext.paintTextWithPaintable(text, paintable, paintableLocation, anchorDirection, gap)
}

/**
 * Paints the bounding box of a paintable at the given location
 */
fun Paintable.strokeBoundingBox(paintingContext: LayerPaintingContext, x: @Window Double, y: @Window Double, showOrigin: Boolean = false, stroke: Color = Color.orange()) {
  val gc = paintingContext.gc
  gc.translate(x, y)

  gc.stroke(stroke)
  gc.strokeRect(boundingBox(paintingContext))

  //Paint the origin
  if (showOrigin) {
    gc.strokeLine(-5.0, 0.0, 5.0, 0.0)
    gc.strokeLine(0.0, -5.0, 0.0, 5.0)
    gc.strokeOvalCenter(0.0, 0.0, 10.0, 10.0)
  }
}

/**
 * Paints a paintable and a text. The paintable is painted beside the text
 */
fun LayerPaintingContext.paintTextWithPaintable(
  text: String,
  paintable: Paintable,
  paintableLocation: PaintableLocation = PaintableLocation.PaintableInside,
  anchorDirection: Direction,
  /**
   * The gap between text and paintable
   */
  @px gap: Double = 5.0,
  @Zoomed maxTextWidth: Double? = null,
) {
  //Check if the paintable is placed on the left side
  val paintableOnLeft = when (paintableLocation) {
    PaintableLocation.PaintableLeft -> true
    PaintableLocation.PaintableRight -> false
    PaintableLocation.PaintableOutside -> anchorDirection.horizontalAlignment != HorizontalAlignment.Right
    PaintableLocation.PaintableInside -> anchorDirection.horizontalAlignment == HorizontalAlignment.Right
  }


  val fontMetrics = gc.getFontMetrics()
  @px val lineHeight = fontMetrics.totalHeight

  val boundingBox = paintable.boundingBox(this)

  //Fix the translation y
  val translateY = when (anchorDirection.verticalAlignment) {
    VerticalAlignment.Top -> max(boundingBox.getHeight() / 2.0, lineHeight / 2.0)
    VerticalAlignment.Center -> 0.0
    VerticalAlignment.Baseline -> lineHeight / 2.0 - fontMetrics.accentLine
    VerticalAlignment.Bottom -> -max(boundingBox.getHeight() / 2.0, lineHeight / 2.0)
  }
  gc.translate(0.0, translateY)


  if (paintableOnLeft) {
    //Paint the paintable first
    gc.saved {
      //The offset depends on the anchor
      val offsetX: Double
      val horizontalAlignment: HorizontalAlignment

      when (anchorDirection.horizontalAlignment) {
        HorizontalAlignment.Left -> {
          offsetX = 0.0
          horizontalAlignment = HorizontalAlignment.Left
        }

        HorizontalAlignment.Center -> {
          offsetX = -gap / 2.0
          horizontalAlignment = HorizontalAlignment.Right
        }

        HorizontalAlignment.Right -> {
          offsetX = -gap - gc.calculateTextWidth(text)
          horizontalAlignment = HorizontalAlignment.Right
        }
      }

      gc.translate(offsetX, 0.0)
      gc.saved {
        paintable.paintInBoundingBox(this, 0.0, 0.0, Direction.get(VerticalAlignment.Center, horizontalAlignment))
      }
    }

    //Paint the text
    gc.saved {
      val textOffsetX: Double
      val horizontalAlignment: HorizontalAlignment

      when (anchorDirection.horizontalAlignment) {
        HorizontalAlignment.Left -> {
          textOffsetX = boundingBox.getWidth() + gap
          horizontalAlignment = HorizontalAlignment.Left
        }

        HorizontalAlignment.Center -> {
          textOffsetX = gap / 2.0
          horizontalAlignment = HorizontalAlignment.Left
        }

        HorizontalAlignment.Right -> {
          textOffsetX = 0.0
          horizontalAlignment = HorizontalAlignment.Right
        }
      }

      gc.translate(textOffsetX, 0.0)
      gc.fillText(text, 0.0, 0.0, Direction.get(VerticalAlignment.Center, horizontalAlignment), maxWidth = maxTextWidth)
    }
  } else {
    //paintable on right
    //Paint the paintable first
    gc.saved {
      //The offset depends on the anchor
      val offsetX: Double
      val horizontalAlignment: HorizontalAlignment

      when (anchorDirection.horizontalAlignment) {
        HorizontalAlignment.Left -> {
          offsetX = gc.calculateTextWidth(text) + gap
          horizontalAlignment = HorizontalAlignment.Left
        }

        HorizontalAlignment.Center -> {
          offsetX = gap / 2.0
          horizontalAlignment = HorizontalAlignment.Left
        }

        HorizontalAlignment.Right -> {
          offsetX = 0.0
          horizontalAlignment = HorizontalAlignment.Right
        }
      }

      gc.translate(offsetX, 0.0)
      gc.saved {
        paintable.paintInBoundingBox(this, 0.0, 0.0, Direction.get(VerticalAlignment.Center, horizontalAlignment))
      }
    }

    //Paint the text
    gc.saved {
      val textOffsetX: Double
      val horizontalAlignment: HorizontalAlignment

      when (anchorDirection.horizontalAlignment) {
        HorizontalAlignment.Left -> {
          textOffsetX = 0.0
          horizontalAlignment = HorizontalAlignment.Left
        }

        HorizontalAlignment.Center -> {
          textOffsetX = -gap / 2.0
          horizontalAlignment = HorizontalAlignment.Right
        }

        HorizontalAlignment.Right -> {
          textOffsetX = -boundingBox.getWidth() - gap
          horizontalAlignment = HorizontalAlignment.Right
        }
      }

      gc.translate(textOffsetX, 0.0)
      gc.fillText(text, 0.0, 0.0, Direction.get(VerticalAlignment.Center, horizontalAlignment))
    }
  }
}

/**
 * Where the [Paintable] is placed
 */
enum class PaintableLocation {
  /**
   * The paintable is always painted on the left side
   */
  PaintableLeft,

  /**
   * The paintable is always painted on the right side
   */
  PaintableRight,

  /**
   * The paintable is always painted on the *inside* side - away from the anchor
   */
  PaintableInside,

  /**
   * The paintable is always painted on the *outside* side - towards the anchor
   */
  PaintableOutside
}

/**
 * Fallback text that is shown instead of a blank text
 */
const val BlankFallbackText: String = "?"

/**
 * Paints a box with a text in it. The anchor direction and gap describes how the box is painted relative
 * to the current "0/0"
 */
fun CanvasRenderingContext.paintTextBox(
  /**
   * The text line to be painted
   */
  line: String,
  /**
   * Describes the anchor direction of the box (*not* the location relative to the anchor)
   */
  anchorDirection: Direction,
  /**
   * The gap that is applied to the anchor (horizontal)
   */
  @px anchorGapHorizontal: Double = 0.0,
  /**
   * The gap that is applied to the anchor (vertical)
   */
  @px anchorGapVertical: Double = anchorGapHorizontal,
  /**
   * The box style
   */
  boxStyle: BoxStyle = BoxStyle.none,
  /**
   * The text color
   */
  textColor: Color = Color.black(),
  /**
   * The max string width
   */
  maxStringWidth: Double = Double.MAX_VALUE,
  /**
   * A callback that adjusts the size of the text-box
   */
  textBoxSizeAdjustment: ((textBox: @px Rectangle, gc: CanvasRenderingContext) -> Size)? = null,
): Rectangle {
  return paintTextBox(
    listOf(line.ifBlank(BlankFallbackText)),
    LineSpacing(1.0),
    HorizontalAlignment.Center,
    anchorDirection,
    anchorGapHorizontal,
    anchorGapVertical,
    boxStyle,
    textColor,
    maxStringWidth,
    textBoxSizeAdjustment
  )
}

/**
 * Paints a box with a text in it. The anchor direction/gap describes how the box is painted relative
 * to the current "0/0"
 *
 * @return the bounds of the box that has been painted
 */
fun CanvasRenderingContext.paintTextBox(
  /**
   * The text lines to be painted
   */
  lines: List<String>,
  /**
   * Describes the anchor direction of the box (*not* the location relative to the anchor)
   *
   * Examples:
   * * if the anchor direction is set to [Direction.TopLeft], the box is painted to the right and below of 0/0
   * * if the anchor direction is set to [Direction.TopRight], the box is painted to the left and below of 0/0
   * * if the anchor direction is set to [Direction.Center], the center of the box is placed directly on 0/0
   */
  anchorDirection: Direction,
  /**
   * The gap that is applied to the anchor
   */
  anchorGapHorizontal: @Zoomed Double = 0.0,

  anchorGapVertical: @Zoomed Double = anchorGapHorizontal,

  /**
   * The box style
   */
  boxStyle: BoxStyle = BoxStyle.none,
  /**
   * The text color
   */
  textColor: Color = Color.black(),
  /**
   * The max string width
   */
  maxStringWidth: Double = Double.MAX_VALUE,
  /**
   * A callback that adjusts the size of the text-box
   */
  textBoxSizeAdjustment: ((textBox: @px Rectangle, gc: CanvasRenderingContext) -> Size)? = null,
): Rectangle {
  return paintTextBox(
    lines = lines,
    lineSpacing = LineSpacing.Single,
    horizontalAlignment = HorizontalAlignment.Left,
    anchorDirection = anchorDirection,
    anchorGapHorizontal = anchorGapHorizontal,
    anchorGapVertical = anchorGapVertical,
    boxStyle = boxStyle,
    textColor = textColor,
    maxStringWidth = maxStringWidth,
    textBoxSizeAdjustment = textBoxSizeAdjustment
  )
}

/**
 * Paints a box with a text in it. The anchor direction/gap describes how the box is painted relative
 * to the current "0/0"
 *
 * @return the bounds of the box that has been painted
 */
fun CanvasRenderingContext.paintTextBox(
  /**
   * The text lines to be painted.
   * At least one line is required!
   */
  lines: List<String>,
  /**
   * The line spacing
   */
  lineSpacing: LineSpacing,
  /**
   * Describes the alignment of the text within the box.
   * Only relevant for multi line text
   */
  horizontalAlignment: HorizontalAlignment,
  /**
   * Describes the anchor direction of the box (*not* the location relative to the anchor)
   *
   * Examples:
   * * if the anchor direction is set to [Direction.TopLeft], the box is painted to the right and below of 0/0
   * * if the anchor direction is set to [Direction.TopRight], the box is painted to the left and below of 0/0
   * * if the anchor direction is set to [Direction.Center], the center of the box is placed directly on 0/0
   */
  anchorDirection: Direction,
  /**
   * The gap that is applied to the anchor - horizontal
   */
  anchorGapHorizontal: @Zoomed Double = 0.0,
  /**
   * The gap that is applied to the anchor - vertical
   */
  anchorGapVertical: @Zoomed Double = anchorGapHorizontal,

  /**
   * The box style
   */
  boxStyle: BoxStyle = BoxStyle.none,
  /**
   * The text color
   */
  textColor: Color = Color.black(),
  /**
   * The max string width
   */
  maxStringWidth: Double = Double.MAX_VALUE,
  /**
   * A callback that adjusts the size of the text-box
   */
  textBoxSizeAdjustment: ((textBox: @px Rectangle, gc: CanvasRenderingContext) -> Size)? = null,
): Rectangle {

  val fixedLines: List<String> = TextLineCalculations.avoidAllBlankLines(lines)

  ///Calculate the text width (does not include padding)
  @px var textBlockWidth = TextLineCalculations.calculateMultilineTextWidth(this, fixedLines, maxStringWidth)

  if (textBlockWidth == 0.0) {
    //No lines with text! just return
    return Rectangle.zero
  }

  //The font metrics for a single line
  @px val fontMetrics = getFontMetrics()
  //The space *between* two lines - the absolute value that is added between each line
  @px val spaceBetweenLines = fontMetrics.totalHeight * lineSpacing.spacePercentage

  //The height of the text block
  @px val textBlockHeight = TextLineCalculations.calculateTextBlockHeight(fontMetrics, fixedLines.size, lineSpacing)

  @px val boxWidth = textBlockWidth + boxStyle.padding.offsetWidth
  @px val boxHeight = textBlockHeight + boxStyle.padding.offsetHeight

  @px val boxOriginXWithoutGapOffset: Double = computeTextBoxX(anchorDirection, boxWidth)
  @px val boxOriginYWithoutGapOffset: Double = computeTextBoxY(anchorDirection, boxHeight, fontMetrics.accentLine, boxStyle.padding.top)

  val offsetForGapX = anchorDirection.calculateOffsetXForGap(anchorGapHorizontal)
  val offsetForGapY = anchorDirection.calculateOffsetYForGap(anchorGapVertical)

  @px val boxOriginX: Double = boxOriginXWithoutGapOffset + offsetForGapX
  @px val boxOriginY: Double = boxOriginYWithoutGapOffset + offsetForGapY
  var boxBounds = Rectangle(boxOriginX, boxOriginY, boxWidth, boxHeight)

  if (textBoxSizeAdjustment != null) {
    val adjustedBoxSize = textBoxSizeAdjustment(boxBounds, this)
    if (adjustedBoxSize != boxBounds.size) {
      boxBounds = Rectangle(boxBounds.location, adjustedBoxSize)
      textBlockWidth = boxBounds.getWidth() - boxStyle.padding.offsetWidth
    }
  }

  //Calculate the x position of the text
  @px val textX: Double = computeTextBoxTextX(anchorDirection, horizontalAlignment, boxStyle.padding, textBlockWidth) + offsetForGapX

  //Calculate the y position of the text - based upon the box -- text baseline is *always* baseline, therefore add the ascent
  val textY: Double = boxOriginY + boxStyle.padding.top + fontMetrics.accentLine

  //Enable shadow - if configured
  boxStyle.shadow?.let {
    shadow(it)
  }

  //Fill the background - of the box
  boxStyle.fill.get()?.let {
    fill(it)
    fillRoundedRect(boxBounds, boxStyle.radii)
  }

  //Always clear the shadow
  clearShadow()


  //Draw the (optional) border
  boxStyle.borderColor.get()?.let { border ->
    stroke(border)
    lineWidth = boxStyle.borderWidth
    strokeRoundedRect(boxBounds, boxStyle.radii)
  }

  var currentTextY = textY
  //Draw the text
  fill(textColor)

  fixedLines.fastForEach { line ->
    fillText(line, textX, currentTextY, horizontalAlignment.toAnchor(), 0.0, 0.0, textBlockWidth)
    currentTextY += fontMetrics.totalHeight + spaceBetweenLines
  }

  return boxBounds
}

/**
 * Calculates the x location of a text box - depending on the anchor direction
 */
private fun computeTextBoxX(anchorDirection: Direction, boxWidth: @px Double): @px Double {
  val boxOriginXWithoutGapOffset: Double
  when (anchorDirection) {
    //Top line:
    //left - center - right

    Direction.TopLeft -> {
      boxOriginXWithoutGapOffset = 0.0
    }

    Direction.TopCenter -> {
      boxOriginXWithoutGapOffset = -boxWidth / 2.0
    }

    Direction.TopRight -> {
      boxOriginXWithoutGapOffset = -boxWidth
    }

    //Center line:
    //left - center - right

    Direction.CenterLeft -> {
      boxOriginXWithoutGapOffset = 0.0
    }

    Direction.Center -> {
      boxOriginXWithoutGapOffset = -boxWidth / 2.0
    }

    Direction.CenterRight -> {
      boxOriginXWithoutGapOffset = -boxWidth
    }

    //Baseline

    Direction.BaseLineLeft -> {
      boxOriginXWithoutGapOffset = 0.0
    }

    Direction.BaseLineCenter -> {
      boxOriginXWithoutGapOffset = -boxWidth / 2.0
    }

    Direction.BaseLineRight -> {
      boxOriginXWithoutGapOffset = -boxWidth
    }

    //Bottom line:
    //left - center - right

    Direction.BottomLeft -> {
      boxOriginXWithoutGapOffset = 0.0
    }

    Direction.BottomCenter -> {
      boxOriginXWithoutGapOffset = -boxWidth / 2.0
    }

    Direction.BottomRight -> {
      boxOriginXWithoutGapOffset = -boxWidth
    }
  }
  return boxOriginXWithoutGapOffset
}

private fun computeTextBoxY(
  anchorDirection: Direction,
  boxHeight: Double,
  accentLine: Double,
  topPadding: Double,
): Double {
  val boxOriginYWithoutGapOffset: Double
  when (anchorDirection) {
    //Top line:
    //left - center - right

    Direction.TopLeft -> {
      boxOriginYWithoutGapOffset = 0.0
    }

    Direction.TopCenter -> {
      boxOriginYWithoutGapOffset = 0.0
    }

    Direction.TopRight -> {
      boxOriginYWithoutGapOffset = 0.0
    }

    //Center line:
    //left - center - right

    Direction.CenterLeft -> {
      boxOriginYWithoutGapOffset = -boxHeight / 2.0
    }

    Direction.Center -> {
      boxOriginYWithoutGapOffset = -boxHeight / 2.0
    }

    Direction.CenterRight -> {
      boxOriginYWithoutGapOffset = -boxHeight / 2.0
    }

    //Baseline

    Direction.BaseLineLeft -> {
      boxOriginYWithoutGapOffset = -accentLine - topPadding
    }

    Direction.BaseLineCenter -> {
      boxOriginYWithoutGapOffset = -accentLine - topPadding
    }

    Direction.BaseLineRight -> {
      boxOriginYWithoutGapOffset = -accentLine - topPadding
    }

    //Bottom line:
    //left - center - right

    Direction.BottomLeft -> {
      boxOriginYWithoutGapOffset = -boxHeight
    }

    Direction.BottomCenter -> {
      boxOriginYWithoutGapOffset = -boxHeight
    }

    Direction.BottomRight -> {
      boxOriginYWithoutGapOffset = -boxHeight
    }
  }

  return boxOriginYWithoutGapOffset
}

/**
 * Returns the x position for a text within a text box
 */
fun computeTextBoxTextX(
  /**
   * The anchor direction
   */
  anchorDirection: Direction,
  /**
   * The horizontal alignment
   */
  horizontalAlignment: HorizontalAlignment,
  /**
   * The padding
   */
  padding: Insets,
  /**
   * The width of the text
   */
  textBlockWidth: Double,
): Double {
  return when (anchorDirection.horizontalAlignment) {
    HorizontalAlignment.Left -> when (horizontalAlignment) {
      HorizontalAlignment.Left -> padding.left
      HorizontalAlignment.Center -> padding.left + textBlockWidth / 2.0
      HorizontalAlignment.Right -> padding.left + textBlockWidth
    }

    HorizontalAlignment.Center -> when (horizontalAlignment) {
      HorizontalAlignment.Left -> -textBlockWidth / 2.0
      HorizontalAlignment.Center -> 0.0
      HorizontalAlignment.Right -> textBlockWidth / 2.0
    }

    HorizontalAlignment.Right -> when (horizontalAlignment) {
      HorizontalAlignment.Left -> -padding.right - textBlockWidth
      HorizontalAlignment.Center -> -padding.right - textBlockWidth / 2.0
      HorizontalAlignment.Right -> -padding.right
    }
  }
}

/**
 * Converts a text alignment to an anchor direction
 */
private fun HorizontalAlignment.toAnchor(): Direction {
  return when (this) {
    HorizontalAlignment.Left -> Direction.BaseLineLeft
    HorizontalAlignment.Right -> Direction.BaseLineRight
    HorizontalAlignment.Center -> Direction.BaseLineCenter
  }
}

/**
 * calculates the x offset for a given gap and anchor direction
 */
@px
fun Direction.calculateOffsetXForGap(gap: Double): Double {
  return horizontalAlignment.calculateOffsetXForGap(gap)
}

/**
 * calculates the x offset for a given gap and horizontal alignment
 */
fun HorizontalAlignment.calculateOffsetXForGap(gap: Double): Double {
  return when (this) {
    HorizontalAlignment.Left -> gap
    HorizontalAlignment.Center -> 0.0
    HorizontalAlignment.Right -> -gap
  }
}

/**
 * calculates the y offset for a given gap and anchor direction
 */
@px
fun Direction.calculateOffsetYForGap(gap: @px Double): Double {
  return verticalAlignment.calculateOffsetYForGap(gap)
}

/**
 * calculates the y offset for a given gap and alignment
 */
fun VerticalAlignment.calculateOffsetYForGap(gap: @px Double): Double {
  return when (this) {
    VerticalAlignment.Top -> gap
    VerticalAlignment.Center -> 0.0
    VerticalAlignment.Baseline -> 0.0
    VerticalAlignment.Bottom -> -gap
  }
}

/**
 * Paints debugging information for a Bézier curve
 */
fun CanvasRenderingContext.debugBezier(bezierCurve: @Window BezierCurve) {
  strokeLine(
    bezierCurve.start,
    bezierCurve.control1
  )
  strokeLine(
    bezierCurve.end,
    bezierCurve.control2
  )

  fillOvalCenter(bezierCurve.start, Size.of(2.0, 2.0))
  fillOvalCenter(bezierCurve.end, Size.of(2.0, 2.0))

  strokeOvalCenter(bezierCurve.control1, Size.of(2.0, 2.0))
  strokeOvalCenter(bezierCurve.control2, Size.of(2.0, 2.0))
}


/**
 * Appends the given rect to the path
 */
fun CanvasRenderingContext.path(bezierCurveRect: @Window BezierCurveRect) {
  moveTo(bezierCurveRect.topCurve.start)
  bezierCurveTo(
    bezierCurveRect.topCurve.control1,
    bezierCurveRect.topCurve.control2,
    bezierCurveRect.topCurve.end
  )

  //
  //The second Bézier curve is painted backwards(!)
  //

  //The line to the end point of the bottom curve
  lineTo((bezierCurveRect.bottomCurve.end))
  bezierCurveTo(
    bezierCurveRect.bottomCurve.control2, //Backwards! Control 2 first
    bezierCurveRect.bottomCurve.control1, //Control 2
    bezierCurveRect.bottomCurve.start //End
  )

  //Close the path by painting a straight line to the top curve
  lineTo(bezierCurveRect.topCurve.start)
}


/**
 * Returns the text size that renders the given text into the given max size.
 * Automatically adjusts the font size on the rendering context.
 *
 * Returns null if the minimum font size does not fit into the given max size
 */
fun CanvasRenderingContext.guessFontSize(
  /**
   * The text that is measured
   */
  text: String,
  /**
   * The max text size.
   */
  maxSize: Size,
  /**
   * The minimum font size.
   * The returned font size is never smaller than this value
   */
  minFontSize: FontSize,
): Size? {
  if (maxSize.isZero()) {
    return null
  }

  var currentTextSize = calculateTextSize(text)

  while (currentTextSize.atLeastOneGreaterThan(maxSize)) {
    val currentFontSize = font.size

    val reducedFontSize = FontSize((currentFontSize.size * 0.9).floor())
    if (reducedFontSize < minFontSize) {
      //Break - does not fit
      return null
    }

    if (reducedFontSize == currentFontSize) {
      throw UnsupportedOperationException("UUps - $reducedFontSize")
    }

    font(FontDescriptorFragment(size = reducedFontSize))
    currentTextSize = calculateTextSize(text)
  }

  return currentTextSize
}
