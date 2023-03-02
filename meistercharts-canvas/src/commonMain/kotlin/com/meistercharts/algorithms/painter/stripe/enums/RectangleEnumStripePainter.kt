package com.meistercharts.algorithms.painter.stripe.enums

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.snapPhysicalTranslation
import com.meistercharts.design.Theme
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.model.Direction

/**
 * Paints stripes using colored (filled) rectangles
 */
class RectangleEnumStripePainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractEnumStripePainter() {

  override val configuration: Configuration = Configuration().also(additionalConfiguration)

  override fun paintSegment(
    paintingContext: LayerPaintingContext,
    startX: @Window Double,
    endX: @Window Double,
    value1ToPaint: @MayBeNoValueOrPending HistoryEnumSet,
    value2ToPaint: @MayBeNoValueOrPending HistoryEnumOrdinal,
    value3ToPaint: Unit,
  ) {
    val valueToPaint: @MayBeNoValueOrPending HistoryEnumSet = value1ToPaint
    val valueMostTimeToPaint: @MayBeNoValueOrPending HistoryEnumOrdinal = value2ToPaint

    val gc = paintingContext.gc

    if (valueToPaint == HistoryEnumSet.NoValue) {
      //the value is NoValue, do *not* paint anything
      return
    }

    //value has changed, paint the rect
    require(startX.isFinite()) {
      "Start value is missing $startX"
    }
    require(endX.isFinite()) {
      "End value is missing $endX"
    }

    @Zoomed val rectangleHeight = paintingVariables().height

    @Zoomed val rectangleWidth = endX - startX

    if (valueToPaint.isNoValue()) {
      if (gc.debug[DebugFeature.HistoryGaps]) {
        gc.fill(Color.red)
        gc.fillRect(startX, 0.0, rectangleWidth, rectangleHeight)
        gc.fill(Color.white)
        gc.fillText("-", startX + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
      return
    }

    if (valueToPaint.isPending()) {
      if (gc.debug[DebugFeature.HistoryGaps]) {
        gc.fill(Color.orange)
        gc.fillRect(startX, 0.0, rectangleWidth, rectangleHeight)
        gc.fill(Color.white)
        gc.fillText("?", startX + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
      return
    }

    //Which ordinal should be painted?
    val ordinalToPaint: HistoryEnumOrdinal = when (configuration.aggregationMode) {
      EnumAggregationMode.ByOrdinal -> valueToPaint.firstSetOrdinal()
      EnumAggregationMode.MostTime -> valueMostTimeToPaint
    }

    gc.fill(configuration.fillProvider(ordinalToPaint, paintingVariables().historyEnum))
    val snapConfiguration = configuration.snapConfiguration()
    gc.snapPhysicalTranslation(snapConfiguration)
    gc.fillRect(startX, 0.0, snapConfiguration.snapXSize(rectangleWidth), snapConfiguration.snapYSize(rectangleHeight))

    //Paint the label
    if (gc.debug[DebugFeature.ShowValues]) {
      gc.fill(Color.white)
      gc.fillText(valueToPaint.toString(), startX + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
    }
  }


  companion object {
    /**
     * Uses ordinal
     */
    fun enumFillProvider(): (value: HistoryEnumOrdinal, historyEnum: HistoryEnum) -> Color {
      return { value, _ ->
        Theme.enumColors().valueAt(value.value)
      }
    }

    /**
     * Uses the state colors.
     */
    fun enumStateFillProvider(): (value: HistoryEnumOrdinal, historyEnum: HistoryEnum) -> Color {
      return { value, _ ->
        Theme.stateColors().valueAt(value.value)
      }
    }
  }

  class Configuration : AbstractEnumStripePainter.Configuration() {
    /**
     * The snap configuration for the stripes
     */
    var snapConfiguration: () -> SnapConfiguration = { SnapConfiguration.OnlyX }

    /**
     * Provides the fill color for the given value
     */
    var fillProvider: (value: HistoryEnumOrdinal, historyEnum: HistoryEnum) -> Color = enumFillProvider()
  }
}
