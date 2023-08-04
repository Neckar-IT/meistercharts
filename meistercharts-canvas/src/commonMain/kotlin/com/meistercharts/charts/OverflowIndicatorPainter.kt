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
package com.meistercharts.charts

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.ArrowHead
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.paintable.Paintable
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import com.meistercharts.resources.svg.PathPaintable
import it.neckar.open.unit.other.px

/**
 * Paints overflow indicators (e.g. for bars).
 */
class OverflowIndicatorPainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) {
  val configuration: Configuration = Configuration().also(additionalConfiguration)

  /**
   * Paints the overflow marker at/to the top
   */
  fun paintOverflowMarkerTop(paintingContext: LayerPaintingContext, contentViewportMinY: @Window @px Double) {
    configuration.topIndicator.paint(paintingContext, 0.0, contentViewportMinY)
  }

  fun paintOverflowMarkerLeft(paintingContext: LayerPaintingContext, contentViewportMinX: @Window @px Double) {
    configuration.leftIndicator.paint(paintingContext, contentViewportMinX, 0.0)
  }

  fun paintOverflowMarkerRight(paintingContext: LayerPaintingContext, contentViewportMaxX: @Window @px Double) {
    configuration.rightIndicator.paint(paintingContext, contentViewportMaxX, 0.0)
  }

  /**
   * Paints the overflow marker at/to the bottom
   */
  fun paintOverflowMarkerBottom(paintingContext: LayerPaintingContext, contentViewportMaxY: @Window @px Double) {
    configuration.bottomIndicator.paint(paintingContext, 0.0, contentViewportMaxY)
  }

  fun paintIndicators(indicatorSelection: MutableOverflowIndicatorsSelection, paintingContext: LayerPaintingContext) {
    paintIndicators(indicatorSelection.top, indicatorSelection.right, indicatorSelection.bottom, indicatorSelection.left, paintingContext)
  }

  fun paintIndicators(top: Boolean, right: Boolean, bottom: Boolean, left: Boolean, paintingContext: LayerPaintingContext) {
    val chartCalculator = paintingContext.chartCalculator

    if (top) {
      paintOverflowMarkerTop(paintingContext, chartCalculator.contentViewportMinY())
    }
    if (right) {
      paintOverflowMarkerRight(paintingContext, chartCalculator.contentViewportMaxX())
    }
    if (bottom) {
      paintOverflowMarkerBottom(paintingContext, chartCalculator.contentViewportMaxY())
    }
    if (left) {
      paintOverflowMarkerLeft(paintingContext, chartCalculator.contentViewportMinX())
    }
  }

  /**
   * Identifies which indicators should be painted
   */
  class MutableOverflowIndicatorsSelection {
    var top: Boolean = false
    var right: Boolean = false
    var bottom: Boolean = false
    var left: Boolean = false

    /**
     * Resets the marker selection to false for all sides
     */
    fun reset() {
      this.top = false
      this.right = false
      this.bottom = false
      this.left = false
    }

    fun updateVertical(y: @Window @px Double, chartCalculator: ChartCalculator) {
      if (chartCalculator.isAboveViewportY(y)) {
        top = true
      }

      if (chartCalculator.isBelowViewportY(y)) {
        bottom = true
      }
    }

    fun updateHorizontal(x: @Window @px Double, chartCalculator: ChartCalculator) {
      if (chartCalculator.isLeftOfViewportX(x)) {
        left = true
      }

      if (chartCalculator.isRightOfViewportX(x)) {
        right = true
      }
    }
  }

  class Configuration {
    /**
     * Applies the default indicators with the provided values
     */
    fun applyDefaultIndicators(fill: Color?, stroke: Color?, strokeWidth: @px Double, arrowHeadLength: @px Double, arrowHeadWidth: @px Double) {
      this.topIndicator = topIndicatorTriangle(fill, stroke, strokeWidth, arrowHeadLength, arrowHeadWidth)
      this.rightIndicator = rightIndicatorTriangle(fill, stroke, strokeWidth, arrowHeadLength, arrowHeadWidth)
      this.bottomIndicator = bottomIndicatorTriangle(fill, stroke, strokeWidth, arrowHeadLength, arrowHeadWidth)
      this.leftIndicator = leftIndicatorTriangle(fill, stroke, strokeWidth, arrowHeadLength, arrowHeadWidth)
    }

    var topIndicator: Paintable = topIndicatorTriangle(Color.darkgray, Color.white)
    var bottomIndicator: Paintable = bottomIndicatorTriangle(Color.darkgray, Color.white)

    var leftIndicator: Paintable = leftIndicatorTriangle(Color.darkgray, Color.white)
    var rightIndicator: Paintable = rightIndicatorTriangle(Color.darkgray, Color.white)
  }

  companion object {
    /**
     * The top indicator triangle (pointing to the top)
     */
    fun topIndicatorTriangle(fill: Color?, stroke: Color?, strokeWidth: @px Double = 1.0, arrowHeadLength: Double = 10.0, arrowHeadWidth: Double = 15.0): PathPaintable = PathPaintable(
      pathActions = ArrowHead.forOrientation(Direction.TopCenter, arrowHeadLength, arrowHeadWidth),
      size = Size(arrowHeadWidth, arrowHeadLength),
      scale = 1.0,
      offset = Distance.of(arrowHeadWidth / 2.0, 0.0),
      fill = fill,
      stroke = stroke,
      strokeWidth = strokeWidth,
      alignmentPoint = Coordinates.of(-arrowHeadWidth / 2.0, 0.0),
    )

    fun leftIndicatorTriangle(fill: Color?, stroke: Color?, strokeWidth: @px Double = 1.0, arrowHeadLength: Double = 10.0, arrowHeadWidth: Double = 15.0): PathPaintable = PathPaintable(
      pathActions = ArrowHead.forOrientation(Direction.CenterLeft, arrowHeadLength, arrowHeadWidth),
      size = Size(arrowHeadLength, arrowHeadWidth),
      scale = 1.0,
      offset = Distance.of(0.0, arrowHeadWidth / 2.0),
      fill = fill,
      stroke = stroke,
      strokeWidth = strokeWidth,
      alignmentPoint = Coordinates.of(0.0, -arrowHeadWidth / 2.0),
    )

    fun rightIndicatorTriangle(fill: Color?, stroke: Color?, strokeWidth: @px Double = 1.0, arrowHeadLength: Double = 10.0, arrowHeadWidth: Double = 15.0): PathPaintable = PathPaintable(
      pathActions = ArrowHead.forOrientation(Direction.CenterRight, arrowHeadLength, arrowHeadWidth),
      size = Size(arrowHeadLength, arrowHeadWidth),
      scale = 1.0,
      offset = Distance.of(arrowHeadLength, arrowHeadWidth / 2.0),
      fill = fill,
      stroke = stroke,
      strokeWidth = strokeWidth,
      alignmentPoint = Coordinates.of(-arrowHeadLength, -arrowHeadWidth / 2.0),
    )

    /**
     * Triangle that points to the bottom
     */
    fun bottomIndicatorTriangle(fill: Color?, stroke: Color?, strokeWidth: @px Double = 1.0, arrowHeadLength: Double = 10.0, arrowHeadWidth: Double = 15.0): PathPaintable = PathPaintable(
      pathActions = ArrowHead.forOrientation(Direction.BottomCenter, arrowHeadLength, arrowHeadWidth),
      size = Size(arrowHeadWidth, arrowHeadLength),
      scale = 1.0,
      offset = Distance.of(arrowHeadWidth / 2.0, arrowHeadLength),
      fill = fill,
      stroke = stroke,
      strokeWidth = strokeWidth,
      alignmentPoint = Coordinates.of(-arrowHeadWidth / 2.0, -arrowHeadLength),
    )
  }
}
