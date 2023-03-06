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
import com.meistercharts.algorithms.painter.BinaryPainter
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending

/**
 * Fills rectangles
 */
class BooleanEnumStripePainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractEnumStripePainter() {

  override val configuration: Configuration = Configuration().also(additionalConfiguration)

  /**
   * The binary painter that is used to paint - initialized with a default value to avoid null checks
   */
  private var binaryPainter: BinaryPainter = BinaryPainter(false, false, 0.0, 0.0, 0.0)

  override fun begin(paintingContext: LayerPaintingContext, height: Double, dataSeriesIndex: EnumDataSeriesIndex, historyConfiguration: HistoryConfiguration) {
    super.begin(paintingContext, height, dataSeriesIndex, historyConfiguration)

    //Additional verifications
    val historyEnum = getHistoryEnum(dataSeriesIndex)

    require(historyEnum.valuesCount == 2) {
      "Only booleans supported (2 options) - but got ${historyEnum.values}"
    }

    historyEnum.values[0].ordinal.let {
      require(it.value == 0) {
        "First ordinal must be 0 but was <${it}>"
      }
    }
    historyEnum.values[1].ordinal.let {
      require(it.value == 1) {
        "Second ordinal must be 0 but was <${it}>"
      }
    }

    //Prepare the binary painter
    binaryPainter = BinaryPainter(false, false, height, paintingContext.width, height)
  }

  override fun paintSegment(
    paintingContext: LayerPaintingContext,
    startX: @Window Double,
    endX: @Window Double,
    value1ToPaint: @MayBeNoValueOrPending HistoryEnumSet,
    value2ToPaint: @MayBeNoValueOrPending HistoryEnumOrdinal,
    value3ToPaint: Unit,
  ) {
    val gc = paintingContext.gc

    if (value1ToPaint.isNoValue()) {
      return
    }

    //value has changed, paint the rect
    require(startX.isFinite()) {
      "Start value is missing $startX"
    }
    require(endX.isFinite()) {
      "End value is missing $endX"
    }

    @Zoomed val height = paintingVariables().height

    val ordinalToPaint: HistoryEnumOrdinal = when (configuration.aggregationMode) {
      EnumAggregationMode.ByOrdinal -> value1ToPaint.firstSetOrdinal()
      EnumAggregationMode.MostTime -> value2ToPaint
    }

    val current = ordinalToPaint == HistoryEnumOrdinal.First

    if (binaryPainter.isPathEmpty()) {
      //Add the first point additionally!
      binaryPainter.addCoordinate(gc, startX, if (current) 0.0 else height)
    }

    binaryPainter.addCoordinate(gc, endX, if (current) 0.0 else height)
  }

  override fun finish(paintingContext: LayerPaintingContext) {
    super.finish(paintingContext)

    requireNotNull(binaryPainter) { "invalid state - no binary painter found" }.finish(paintingContext.gc)
  }

  class Configuration : AbstractEnumStripePainter.Configuration() {
    /**
     * The snap configuration for the stripes
     */
    var snapConfiguration: () -> SnapConfiguration = { SnapConfiguration.OnlyX }
  }
}
