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
package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.resolve
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.snapPhysicalTranslation
import com.meistercharts.design.Theme
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.model.Direction
import it.neckar.open.formatting.intFormat

/**
 * Paints stripes using colored (filled) rectangles
 */
class RectangleReferenceEntryStripePainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractReferenceEntryStripePainter() {

  override val configuration: Configuration = Configuration().also(additionalConfiguration)

  override fun paintSegment(
    paintingContext: LayerPaintingContext,
    startX: @Window Double, //might be out of the screen
    endX: @Window Double, //might be out of the screen
    value1ToPaint: @MayBeNoValueOrPending ReferenceEntryId,
    value2ToPaint: @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount,
    value3ToPaint: @MayBeNoValueOrPending HistoryEnumSet,
    value4ToPaint: ReferenceEntryData?,
  ) {
    @MayBeNoValueOrPending val idToPaint: ReferenceEntryId = value1ToPaint
    @Suppress("UnnecessaryVariable") @MayBeNoValueOrPending val count = value2ToPaint
    //TODO what to do with the HistoryEnumSet?
    @Suppress("UnnecessaryVariable") val entryData = value4ToPaint

    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    if (idToPaint == ReferenceEntryId.NoValue) {
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

    val paintingVariables = paintingVariables()

    val startXinViewport = chartCalculator.coerceInViewportX(startX)
    val endXinViewport = chartCalculator.coerceInViewportX(endX)

    @Zoomed val rectangleHeight = paintingVariables.height
    @Zoomed val rectangleWidth = endXinViewport - startXinViewport

    if (idToPaint.isNoValue()) {
      if (gc.debug[DebugFeature.HistoryGaps]) {
        gc.fill(Color.red)
        gc.fillRect(startXinViewport, 0.0, rectangleWidth, rectangleHeight)
        gc.fill(Color.white)
        gc.fillText("-", startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
      return
    }

    if (idToPaint.isPending()) {
      if (gc.debug[DebugFeature.HistoryGaps]) {
        gc.fill(Color.orange)
        gc.fillRect(startXinViewport, 0.0, rectangleWidth, rectangleHeight)
        gc.fill(Color.white)
        gc.fillText("?", startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
      return
    }

    when {
      count.value > 1 -> {
        //Count is > 1, we show the number of entries
        gc.fill(Color.silver)

        val snapConfiguration = configuration.snapConfiguration()
        gc.snapPhysicalTranslation(snapConfiguration)
        gc.fillRect(startXinViewport, 0.0, snapConfiguration.snapXSize(rectangleWidth - 2.0), snapConfiguration.snapYSize(rectangleHeight))
        //gc.strokeOvalCenter(startX + rectangleWidth / 2.0, rectangleHeight / 2.0, 20.0, 20.0)

        gc.fill(configuration.countLabelColor)
        gc.fillText(intFormat.format(count.value.toDouble()), startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }

      else -> {
        //Count is <= 1
        gc.fill(configuration.fillProvider(idToPaint, paintingVariables.historyConfiguration))

        val snapConfiguration = configuration.snapConfiguration()
        gc.snapPhysicalTranslation(snapConfiguration)
        gc.fillRect(startXinViewport, 0.0, snapConfiguration.snapXSize(rectangleWidth), snapConfiguration.snapYSize(rectangleHeight))

        //Paint the label
        entryData?.label?.resolve(paintingContext)?.let {
          gc.fill(configuration.labelColor)
          gc.fillText(it, startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
        }
      }
    }
  }

  class Configuration : AbstractReferenceEntryStripePainter.Configuration() {
    /**
     * The snap configuration for the stripes
     */
    var snapConfiguration: () -> SnapConfiguration = { SnapConfiguration.OnlyX }

    /**
     * Provides the fill color for the given value
     */
    var fillProvider: (value: ReferenceEntryId, historyConfiguration: HistoryConfiguration) -> Color = { value: ReferenceEntryId, historyConfiguration: HistoryConfiguration ->
      Theme.chartColors().valueAt(value.id)
    }

    /**
     * The color of the label
     */
    var labelColor: Color = Color.white

    /**
     * The color when the count is shown
     */
    var countLabelColor: Color = Color.gray
  }
}
