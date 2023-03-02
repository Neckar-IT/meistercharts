package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter.ValueLabelAnchorDirectionProvider.Companion.barPointsLeft
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter.ValueLabelAnchorDirectionProvider.Companion.barPointsUp
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Direction
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
