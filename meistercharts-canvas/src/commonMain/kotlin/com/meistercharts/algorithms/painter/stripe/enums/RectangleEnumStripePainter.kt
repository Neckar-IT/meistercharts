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
package com.meistercharts.algorithms.painter.stripe.enums

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.snapPhysicalTranslation
import com.meistercharts.design.Theme
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.model.Direction
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Paints stripes using colored (filled) rectangles
 */
class RectangleEnumStripePainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractEnumStripePainter() {

  override val configuration: Configuration = Configuration().also(additionalConfiguration)

  override fun paintSegment(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: EnumDataSeriesIndex,
    startX: @Window Double,
    endX: @Window Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    value1ToPaint: @MayBeNoValueOrPending HistoryEnumSet,
    value2ToPaint: @MayBeNoValueOrPending HistoryEnumOrdinal,
    value3ToPaint: Unit,
    value4ToPaint: Unit,
  ) {
    val valueToPaint: @MayBeNoValueOrPending HistoryEnumSet = value1ToPaint
    val valueMostTimeToPaint: @MayBeNoValueOrPending HistoryEnumOrdinal = value2ToPaint

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

    val gc = paintingContext.gc

    @Zoomed val rectangleHeight = paintingVariables(dataSeriesIndex).height
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

    val paintingVariables: EnumStripePainterPaintingVariables = paintingVariables()
    paintingVariables.historyConfiguration

    gc.fill(configuration.fillProvider(ordinalToPaint, paintingVariables.historyEnum))
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
