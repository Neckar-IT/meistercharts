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

import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter.ValueLabelAnchorDirectionProvider.Companion.barPointsLeft
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter.ValueLabelAnchorDirectionProvider.Companion.barPointsUp
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintMark
import com.meistercharts.model.Direction

/**
 * Ensures that the value-label is always within the window.
 * Places the label outside the bar - if there is enough room. Inside the bar if necessary.
 * Please ensure that the label is still visible when placed inside the bar.
 */
object FlippingValueLabelAnchorDirectionProvider : GroupedBarsPainter.ValueLabelAnchorDirectionProvider {
  override fun directionInBarToAnchorPointHorizontal(
    axisOrientation: AxisOrientationX,
    barValue: Double,
    barLabel: String,
    barLabelWidth: Double,
    barSize: Double,
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
  ): Direction {
    if (barPointsLeft(barValue, axisOrientation)) {
      return Direction.CenterLeft
    }

    return Direction.CenterRight
  }

  override fun directionInBarToAnchorPointVertical(
    axisOrientation: AxisOrientationY,
    barValue: Double,
    barLabel: String,
    barLabelWidth: Double,
    barSize: Double,
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
  ): Direction {
    if (barPointsUp(barValue, axisOrientation)) {
      return Direction.TopCenter
    }
    return Direction.BottomCenter
  }

  override fun anchorDirectionHorizontal(
    axisOrientation: AxisOrientationX,
    barValue: Double,
    barLabel: String,
    barLabelWidth: Double,
    barSize: Double,
    anchorX: Double,
    anchorY: Double,
    anchorGapHorizontal: Double,
    anchorGapVertical: Double,
    paintingContext: LayerPaintingContext
  ): Direction {
    return if (barPointsLeft(barValue, axisOrientation)) {
      anchorDirectionForHorizontalBarsPointingLeft(
        barLabelWidth = barLabelWidth,
        barSize = barSize,
        anchorGapHorizontal = anchorGapHorizontal,
        anchorGapVertical = anchorGapVertical,
        anchorX = anchorX,
        anchorY = anchorY,
        paintingContext = paintingContext
      )
    } else {
      anchorDirectionForHorizontalBarsPointingRight(
        barLabelWidth = barLabelWidth,
        barSize = barSize,
        anchorGapHorizontal = anchorGapHorizontal,
        anchorGapVertical = anchorGapVertical,
        anchorX = anchorX,
        anchorY = anchorY,
        paintingContext = paintingContext
      )
    }
  }

  override fun anchorDirectionVertical(
    axisOrientation: AxisOrientationY,
    barValue: Double,
    barLabel: String,
    barLabelWidth: Double,
    barSize: Double,
    anchorX: Double,
    anchorY: Double,
    anchorGapHorizontal: Double,
    anchorGapVertical: Double,
    paintingContext: LayerPaintingContext
  ): Direction {
    return if (barPointsUp(barValue, axisOrientation)) {
      anchorDirectionForVerticalBarsPointingUp(
        anchorGapHorizontal = anchorGapHorizontal,
        anchorGapVertical = anchorGapVertical,
        anchorX = anchorX,
        anchorY = anchorY,
        paintingContext = paintingContext
      )
    } else {
      anchorDirectionForVerticalBarsPointingDown(
        anchorGapHorizontal = anchorGapHorizontal,
        anchorGapVertical = anchorGapVertical,
        anchorX = anchorX,
        anchorY = anchorY,
        paintingContext = paintingContext
      )
    }
  }

  /**
   * Calculates the anchor direction for horizontal bars with positive values
   */
  @Suppress("UNUSED_PARAMETER")
  private fun anchorDirectionForHorizontalBarsPointingRight(
    barLabelWidth: @Zoomed Double,
    barSize: @Zoomed Double,
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
    anchorX: @Window Double,
    anchorY: @Window Double,
    paintingContext: LayerPaintingContext
  ): Direction {
    val gc = paintingContext.gc
    @Window val maxX = paintingContext.chartCalculator.contentAreaRelative2windowX(1.0) - anchorGapHorizontal - barLabelWidth
    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      Arrows.to(Direction.CenterLeft, 0.0).fill(gc, anchorX, anchorY)
      Arrows.to(Direction.CenterRight, 0.0).fill(gc, maxX, anchorY)
      gc.strokeLine(anchorX, anchorY, maxX, anchorY)
    }
    return if (anchorX <= maxX) {
      Direction.CenterLeft
    } else {
      Direction.CenterRight
    }
  }

  private fun anchorDirectionForHorizontalBarsPointingLeft(
    barLabelWidth: @Zoomed Double,
    barSize: @Zoomed Double,
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
    anchorX: @Window Double,
    anchorY: @Window Double,
    paintingContext: LayerPaintingContext
  ): Direction {
    val gc = paintingContext.gc
    val minX = paintingContext.chartCalculator.contentAreaRelative2windowX(0.0) + anchorGapHorizontal + barLabelWidth
    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      Arrows.to(Direction.CenterRight, 0.0).fill(gc, anchorX, anchorY)
      Arrows.to(Direction.CenterLeft, 0.0).fill(gc, minX, anchorY)
      gc.strokeLine(anchorX, anchorY, minX, anchorY)
    }
    return if (anchorX >= minX) {
      Direction.CenterRight
    } else {
      Direction.CenterLeft
    }
  }

  private fun anchorDirectionForVerticalBarsPointingUp(
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
    anchorX: @Window Double,
    anchorY: @Window Double,
    paintingContext: LayerPaintingContext
  ): Direction {
    val gc = paintingContext.gc
    val textHeight = gc.getFontMetrics().totalHeight
    val minY = paintingContext.chartCalculator.contentAreaRelative2windowY(0.0) + anchorGapVertical + textHeight
    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      gc.paintMark(anchorX, anchorY)
      gc.strokeLine(anchorX, anchorY, anchorX, minY)
    }
    return if (anchorY >= minY) {
      Direction.BottomCenter
    } else {
      Direction.TopCenter
    }
  }

  private fun anchorDirectionForVerticalBarsPointingDown(
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
    anchorX: @Window Double,
    anchorY: @Window Double,
    paintingContext: LayerPaintingContext
  ): Direction {
    val gc = paintingContext.gc
    val textHeight = gc.getFontMetrics().totalHeight
    val maxY = paintingContext.chartCalculator.contentAreaRelative2windowY(1.0) - anchorGapVertical - textHeight
    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      gc.paintMark(anchorX, anchorY)
      gc.strokeLine(anchorX, anchorY, anchorX, maxY)
    }
    return if (anchorY <= maxY) {
      Direction.TopCenter
    } else {
      Direction.BottomCenter
    }
  }
}
