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
package com.meistercharts.algorithms.tooltip.balloon

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.ArcPathWorkaroundEpsilon
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.AbstractPaintable
import com.meistercharts.canvas.paintable.AbstractPaintablePaintingVariables
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.PaintablePaintingVariables
import com.meistercharts.color.get
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.unit.number.NegativeOrZero
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.absoluteValue

/**
 * Paints a balloon tooltip.
 *
 * The provided content [Paintable] is *always* painted from the top left corner of the tooltip.
 */
class BalloonTooltipPaintable(
  content: Paintable,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  val configuration: Configuration = Configuration(content).also(additionalConfiguration)

  /**
   * Updates the content - and calls [layout]
   */
  fun layout(paintingContext: LayerPaintingContext, content: Paintable) {
    configuration.content = content
    layout(paintingContext)
  }

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  /**
   * The painting variables for the balloon tooltip - and content
   */
  private val paintingVariables = object : AbstractPaintablePaintingVariables() {
    /**
     * The total width - inclusive content padding
     */
    @px
    var widthInclusiveContentPadding: Double = Double.NaN

    /**
     * The total height - inclusive content padding
     */
    @px
    var heightInclusiveContentPadding: Double = Double.NaN

    /**
     * The height to the top (low y values).
     * Must only be used when the nose is on the left/right.
     *
     * This value is usually negative.
     *
     * Does include content margin
     */
    @px
    var heightLow: @Relative("nose") @NegativeOrZero Double = Double.NaN

    /**
     * The height to the bottom (high y values)
     * Must only be used when the nose is on the left/right
     *
     * Does include content margin
     */
    @px
    var heightHigh: @Relative("nose") @PositiveOrZero Double = Double.NaN

    /**
     * The width to the left (low x values)
     * Must only be used when the nose is on the top/bottom
     *
     * This value is usually negative.
     *
     * Does include content margin
     */
    @px
    var widthLow: @Relative("nose") @NegativeOrZero Double = Double.NaN

    /**
     * The width to the right (high x values)
     * Must only be used when the nose is on the top/bottom
     *
     * Does include content margin
     */
    @px
    var widthHigh: @Relative("nose") @PositiveOrZero Double = Double.NaN

    /**
     * The side, where the nose is placed at
     */
    var noseSide: Side = Side.Left

    var noseWidthHalf: @px Double = Double.NaN

    /**
     * The size of the content.
     */
    var contentBoundingBox: @Zoomed Rectangle = Rectangle.zero

    /**
     * The padding around the content
     */
    var contentPadding: @Zoomed Insets = Insets.empty

    var contentOffsetX: @px Double = Double.NaN
    var contentOffsetY: @px Double = Double.NaN

    var radiusTopLeft: @px Double = Double.NaN
    var radiusTopRight: @px Double = Double.NaN
    var radiusBottomRight: @px Double = Double.NaN
    var radiusBottomLeft: @px Double = Double.NaN


    var xLeft: @px Double = Double.NaN
    var xRight: @px Double = Double.NaN
    var xCenter: @px Double = Double.NaN
    var yCenter: @px Double = Double.NaN
    var yTop: @px Double = Double.NaN
    var yBottom: @px Double = Double.NaN

    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      contentBoundingBox = configuration.content.boundingBox(paintingContext)
      val contentSize = contentBoundingBox.size
      contentPadding = configuration.boxStyle.padding

      noseSide = configuration.noseSide()

      //The distance from top left
      @px val nosePositionDistanceFromTop = configuration.nosePositionCalculator.calculateDistanceFromTop(contentSize)
      @px val nosePositionDistanceFromLeft = configuration.nosePositionCalculator.calculateDistanceFromLeft(contentSize)


      noseWidthHalf = configuration.noseWidth / 2.0

      //Calculate the total width/height - including the content padding
      widthInclusiveContentPadding = contentSize.width + contentPadding.offsetWidth
      heightInclusiveContentPadding = contentSize.height + contentPadding.offsetHeight

      //The heights of the tooltip - relative to the nose location
      heightLow = -nosePositionDistanceFromTop - contentPadding.top
      heightHigh = contentSize.height - nosePositionDistanceFromTop + contentPadding.bottom

      //the widths of the tooltip - relative to the nose location
      widthLow = -nosePositionDistanceFromLeft - contentPadding.left
      widthHigh = contentSize.width - nosePositionDistanceFromLeft + contentPadding.right


      contentOffsetX = when (noseSide) {
        Side.Left -> configuration.noseLength + contentPadding.left
        Side.Right -> -configuration.noseLength - contentPadding.right - contentSize.width

        Side.Bottom,
        Side.Top,
        -> -nosePositionDistanceFromLeft
      }

      contentOffsetY = when (noseSide) {
        Side.Left,
        Side.Right,
        -> -nosePositionDistanceFromTop

        Side.Top -> configuration.noseLength + contentPadding.top
        Side.Bottom -> -configuration.noseLength - contentPadding.bottom - contentSize.height
      }

      //The radii for the border
      val borderRadii = configuration.boxStyle.radii ?: BorderRadius.none

      //Calculate the max radii - just for content width/height (without calculating the nose location)
      val borderRadiusMaxWidthHeight = (widthInclusiveContentPadding / 2.0).coerceAtMost(heightInclusiveContentPadding / 2.0)

      radiusTopLeft = borderRadii.topLeft.coerceAtMost(borderRadiusMaxWidthHeight)
      radiusTopRight = borderRadii.topRight.coerceAtMost(borderRadiusMaxWidthHeight)
      radiusBottomLeft = borderRadii.bottomLeft.coerceAtMost(borderRadiusMaxWidthHeight)
      radiusBottomRight = borderRadii.bottomRight.coerceAtMost(borderRadiusMaxWidthHeight)

      //Calculate the start and end of the box
      when (noseSide) {
        Side.Left -> {
          xLeft = configuration.noseLength
          xRight = widthInclusiveContentPadding + configuration.noseLength

          yTop = heightLow
          yBottom = heightHigh

          radiusTopLeft = radiusTopLeft.coerceAtMost(heightLow.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
          radiusBottomLeft = radiusBottomLeft.coerceAtMost(heightHigh.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
        }

        Side.Right -> {
          xLeft = -widthInclusiveContentPadding - configuration.noseLength
          xRight = -configuration.noseLength

          yTop = heightLow
          yBottom = heightHigh

          radiusTopRight = radiusTopRight.coerceAtMost(heightLow.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
          radiusBottomRight = radiusBottomRight.coerceAtMost(heightHigh.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
        }

        Side.Top -> {
          xRight = widthHigh
          xLeft = widthLow

          yTop = configuration.noseLength
          yBottom = configuration.noseLength + heightInclusiveContentPadding

          radiusTopLeft = radiusTopLeft.coerceAtMost(widthLow.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
          radiusTopRight = radiusTopRight.coerceAtMost(widthHigh.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
        }

        Side.Bottom -> {
          xRight = widthHigh
          xLeft = widthLow

          yTop = -configuration.noseLength - heightInclusiveContentPadding
          yBottom = -configuration.noseLength

          radiusBottomLeft = radiusBottomLeft.coerceAtMost(widthLow.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
          radiusBottomRight = radiusBottomRight.coerceAtMost(widthHigh.absoluteValue - noseWidthHalf - ArcPathWorkaroundEpsilon)
        }
      }

      xCenter = (xRight + xLeft) / 2.0
      yCenter = (yTop + yBottom) / 2.0

      boundingBox = Rectangle(-contentPadding.left, -contentPadding.top, widthInclusiveContentPadding, heightInclusiveContentPadding)
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.beginPath()
    //starting at nose tip
    gc.moveTo(0.0, 0.0)

    with(paintingVariables) {

      /**
       * Always paint clockwise - the arcTo methods should always be the same then - just in different order
       */
      when (noseSide) {
        Side.Left -> {
          gc.lineTo(xLeft, -noseWidthHalf) //top nose start

          gc.arcTo(xLeft, yTop, xCenter, yTop, paintingVariables.radiusTopLeft) //To top center
          gc.arcTo(xRight, yTop, xRight, yCenter, paintingVariables.radiusTopRight) //To right center
          gc.arcTo(xRight, yBottom, xCenter, yBottom, paintingVariables.radiusBottomRight) //To bottom center
          gc.arcTo(xLeft, yBottom, xLeft, yCenter, paintingVariables.radiusBottomLeft) //To bottom center

          gc.lineTo(xLeft, noseWidthHalf) //bottom nose start
          gc.closePath()
        }

        Side.Right -> {
          gc.lineTo(xRight, noseWidthHalf) //bottom nose start

          gc.arcTo(xRight, yBottom, xCenter, yBottom, paintingVariables.radiusBottomRight) //To bottom center
          gc.arcTo(xLeft, yBottom, xLeft, yCenter, paintingVariables.radiusBottomLeft) //To left center
          gc.arcTo(xLeft, yTop, xCenter, yTop, paintingVariables.radiusTopLeft) //To top center
          gc.arcTo(xRight, yTop, xRight, yCenter, paintingVariables.radiusTopRight) //To right center

          gc.lineTo(xRight, -noseWidthHalf) //top nose start
          gc.closePath()
        }

        Side.Bottom -> {
          gc.lineTo(-noseWidthHalf, yBottom) //right nose start

          gc.arcTo(xLeft, yBottom, xLeft, yCenter, paintingVariables.radiusBottomLeft) //To left center
          gc.arcTo(xLeft, yTop, xCenter, yTop, paintingVariables.radiusTopLeft) //To top center
          gc.arcTo(xRight, yTop, xRight, yCenter, paintingVariables.radiusTopRight) //To right center
          gc.arcTo(xRight, yBottom, xCenter, yBottom, paintingVariables.radiusBottomRight) //To bottom center

          gc.lineTo(noseWidthHalf, yBottom) //left nose start
          gc.closePath()
        }

        Side.Top -> {
          gc.lineTo(noseWidthHalf, yTop) //right nose start

          gc.arcTo(xRight, yTop, xRight, yCenter, paintingVariables.radiusTopRight) //To right center
          gc.arcTo(xRight, yBottom, xCenter, yBottom, paintingVariables.radiusBottomRight) //To bottom center
          gc.arcTo(xLeft, yBottom, xLeft, yCenter, paintingVariables.radiusBottomLeft) //To left center
          gc.arcTo(xLeft, yTop, xCenter, yTop, paintingVariables.radiusTopLeft) //To top center

          gc.lineTo(-noseWidthHalf, yTop) //left nose start
          gc.closePath()
        }
      }

      //Fill the background - including shadow
      configuration.boxStyle.shadow?.let { shadow ->
        gc.shadow(shadow)
      }
      configuration.boxStyle.fill.get()?.let { fill ->
        gc.fill(fill)
        gc.fill()
      }
      gc.clearShadow()

      //Stroke the border
      configuration.boxStyle.borderColor.get()?.let { borderColor ->
        gc.lineWidth = configuration.boxStyle.borderWidth
        gc.stroke(borderColor)
        gc.stroke()
      }

      //Paint the content
      if (gc.debug[DebugFeature.ShowAnchors]) {
        gc.paintMark(paintingVariables.contentOffsetX, paintingVariables.contentOffsetY)
      }

      configuration.content.paintInBoundingBox(
        paintingContext,
        x = paintingVariables.contentOffsetX,
        y = paintingVariables.contentOffsetY,
        direction = Direction.TopLeft,
        gapHorizontal = 0.0,
        gapVertical = 0.0
      )
    }
  }

  class Configuration(
    /**
     * The content paintable for the balloon tooltip
     */
    var content: Paintable,
  ) {
    /**
     * The direction of the nose
     */
    var noseSide: () -> Side = { Side.Left }

    /**
     * The width of the nose
     */
    var noseWidth: @px Double = 10.0

    /**
     * The length of the nose.
     * This is the distance of the point of interest and the start of the box
     */
    var noseLength: @px Double = 10.0

    /**
     * The percentage where the nose is positioned.
     * Between 0.0 (does not make any sense) and 1.0 (does not make any sense).
     * Default is 50%.
     *
     * Attention: The nose position does *not* take the content padding  of the [boxStyle] into account.
     * The nose position is always relative to the content.
     */
    fun relativeNosePosition(relativeNosePosition: @pct Double) {
      this.nosePositionCalculator = RelativeNosePosition(relativeNosePosition)
    }

    /**
     * Sets the *absolute* nose position.
     */
    fun absoluteNosePosition(
      absoluteNosePosition: @px Double,
      /**
       * The direction, the absolute node position is applied from.
       * Attention: Only one orientation (vertical or horizontal) is used - depending on the side of the nose.
       */
      direction: Direction,
    ) {
      this.nosePositionCalculator = AbsoluteNosePositionCalculator(absoluteNosePosition, direction)
    }

    /**
     * Calculates the nose position
     */
    var nosePositionCalculator: NosePositionCalculator = NosePosition50Pct

    /**
     * The box style for the tooltip
     */
    var boxStyle: BoxStyle = BoxStyle.balloon
  }
}

/**
 * Calculates the nose position.
 * Based on the content bounding box and the content margin.
 */
interface NosePositionCalculator {
  /**
   * Returns the distance from the top side.
   * Depending on the nose direction.
   *
   * Does *not* include content margin
   */
  fun calculateDistanceFromTop(
    contentSize: @Zoomed Size,
  ): @Zoomed Double

  /**
   * Returns the distance from the left side.
   * Depending on the nose direction.
   *
   * Does *not* include content margin
   */
  fun calculateDistanceFromLeft(
    contentSize: @Zoomed Size,
  ): @Zoomed Double
}

/**
 * Returns 0.0 - should only be used as fallback or for tests
 */
object NosePositionTopLeft : NosePositionCalculator {
  override fun calculateDistanceFromTop(contentSize: @Zoomed Size): Double {
    return 0.0
  }

  override fun calculateDistanceFromLeft(contentSize: Size): Double {
    return 0.0
  }
}

/**
 * Places the nose position in the center of the balloon
 */
val NosePosition50Pct: RelativeNosePosition = RelativeNosePosition(0.5)

class RelativeNosePosition(val relativeNosePosition: @pct Double) : NosePositionCalculator {
  override fun calculateDistanceFromTop(contentSize: @Zoomed Size): Double {
    return contentSize.height * relativeNosePosition
  }

  override fun calculateDistanceFromLeft(contentSize: Size): Double {
    return contentSize.width * relativeNosePosition
  }
}

/**
 * Calculates the nose position using a direction and an absolute value
 */
class AbsoluteNosePositionCalculator(
  val absoluteNosePosition: @Zoomed Double,
  val direction: Direction,
) : NosePositionCalculator {
  override fun calculateDistanceFromTop(contentSize: @Zoomed Size): Double {
    return when (direction.verticalAlignment) {
      VerticalAlignment.Top -> absoluteNosePosition
      VerticalAlignment.Baseline,
      VerticalAlignment.Center,
      -> contentSize.height / 2.0 + absoluteNosePosition

      VerticalAlignment.Bottom -> contentSize.height - absoluteNosePosition
    }
  }

  override fun calculateDistanceFromLeft(contentSize: Size): Double {
    return when (direction.horizontalAlignment) {
      HorizontalAlignment.Left -> absoluteNosePosition
      HorizontalAlignment.Center -> contentSize.width / 2.0 + absoluteNosePosition
      HorizontalAlignment.Right -> contentSize.width - absoluteNosePosition
    }
  }
}
