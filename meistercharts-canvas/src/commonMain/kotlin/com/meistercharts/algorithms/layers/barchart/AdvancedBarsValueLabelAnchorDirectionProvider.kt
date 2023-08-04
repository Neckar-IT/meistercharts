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

import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter.ValueLabelAnchorDirectionProvider.Companion.barPointsLeft
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter.ValueLabelAnchorDirectionProvider.Companion.barPointsUp
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Direction
import kotlin.math.absoluteValue

/**
 * Alternative implementation. Puts the labels above the horizontal bars
 *
 * Default implementation that ensures the label is always within the windows.
 * Paints the label outside the bar - if there is enough room. Inside the bar if necessary.
 */
object AdvancedBarsValueLabelAnchorDirectionProvider : GroupedBarsPainter.ValueLabelAnchorDirectionProvider {
  override fun directionInBarToAnchorPointHorizontal(
    axisOrientation: AxisOrientationX, barValue: Double, barLabel: String, barLabelWidth: Double, barSize: Double,
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
  ): Direction {
    val barLabelSmallerThanBar = barLabelSmallerThanBar(barLabelWidth, anchorGapHorizontal, barSize)

    if (barPointsLeft(barValue, axisOrientation) xor barLabelSmallerThanBar) {
      //label is small enough -> right aligned
      return Direction.TopRight
    }

    //Label is too large / bar is too short - left aligned
    return Direction.TopLeft
  }

  private fun barLabelSmallerThanBar(
    barLabelWidth: Double,
    anchorGapHorizontal: @Zoomed Double,
    barSize: Double
  ) = barLabelWidth + anchorGapHorizontal <= barSize.absoluteValue

  override fun directionInBarToAnchorPointVertical(
    axisOrientation: AxisOrientationY, barValue: Double, barLabel: String, barLabelWidth: Double, barSize: Double,
    anchorGapHorizontal: @Zoomed Double,
    anchorGapVertical: @Zoomed Double,
  ): Direction {
    if (barPointsUp(barValue, axisOrientation)) {
      return Direction.TopCenter
    }
    return Direction.BottomCenter
  }

  override fun anchorDirectionHorizontal(axisOrientation: AxisOrientationX, barValue: Double, barLabel: String, barLabelWidth: Double, barSize: Double, anchorX: Double, anchorY: Double, anchorGapHorizontal: Double, anchorGapVertical: Double, paintingContext: LayerPaintingContext): Direction {
    val barLabelSmallerThanBar = barLabelSmallerThanBar(barLabelWidth, anchorGapHorizontal, barSize)

    return if (barPointsLeft(barValue, axisOrientation) xor barLabelSmallerThanBar) {
      Direction.BottomRight
    } else {
      Direction.BottomLeft
    }
  }

  override fun anchorDirectionVertical(axisOrientation: AxisOrientationY, barValue: Double, barLabel: String, barLabelWidth: Double, barSize: Double, anchorX: Double, anchorY: Double, anchorGapHorizontal: Double, anchorGapVertical: Double, paintingContext: LayerPaintingContext): Direction {
    return if (barPointsUp(barValue, axisOrientation)) {
      Direction.BottomCenter
    } else {
      Direction.TopCenter
    }
  }
}
