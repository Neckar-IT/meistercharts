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
package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.PaintLoopIndexCondition
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Abstract base class for stripe painters.
 *
 * A stripe painter paints a (horizontal) stripe with several stripe segments.
 * A segment might span multiple "value segments". This allows optimized painting of a single area for multiple recordings of the same value.
 */
abstract class AbstractStripePainter<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type> : StripePainter<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type> {
  /**
   * Returns the painting variables for this stripe painter
   */
  abstract fun paintingVariables(): StripePainterPaintingVariables<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type>

  /**
   * The last data series index
   */
  private val sameDataSeriesIndexCondition = PaintLoopIndexCondition.isEqualInt()

  override fun layoutBegin(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    sameDataSeriesIndexCondition.verifySame(paintingContext.loopIndex, dataSeriesIndex.value) {
      "Stripe painters must be instantiated for each data series. Do not reuse them"
    }

    val paintingVariables = paintingVariables()
    paintingVariables.prepareLayout(paintingContext, height, dataSeriesIndex, historyConfiguration)
  }

  override fun layoutValueChange(
    paintingContext: LayerPaintingContext,
    startX: @Window Double,
    endX: @Window Double,
    startTime: @ms Double,
    endTime: @ms Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    newValue1: Value1Type,
    newValue2: Value2Type,
    newValue3: Value3Type,
    newValue4: Value4Type,
  ): Double {

    val paintingVariables = paintingVariables()

    if (relevantValuesHaveChanged(newValue1, newValue2, newValue3, newValue4).not()) {
      //Values have not changed, just update the end - but do not paint
      paintingVariables.currentEndX = endX
      paintingVariables.currentEndTime = endTime
      return Double.NaN
    }

    relevantValuesChanged(paintingVariables, newValue1, newValue2, newValue3, newValue4, startX, endX, startTime, endTime, activeTimeStamp)

    return layoutSegment(paintingContext)
  }

  /**
   * Is called if the relevant values have changed
   */
  protected open fun relevantValuesChanged(
    paintingVariables: StripePainterPaintingVariables<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type>,
    newValue1: Value1Type,
    newValue2: Value2Type,
    newValue3: Value3Type,
    newValue4: Value4Type,
    startX: @Window Double,
    endX: @Window Double,
    startTime: @ms Double,
    endTime: @ms Double,
    activeTimeStamp: @ms @MayBeNaN Double,
  ) {
    //Remember the updated properties - for the next paint
    paintingVariables.nextValue1 = newValue1
    paintingVariables.nextValue2 = newValue2
    paintingVariables.nextValue3 = newValue3
    paintingVariables.nextValue4 = newValue4

    paintingVariables.nextStartX = startX
    paintingVariables.nextEndX = endX
    paintingVariables.nextStartTime = startTime
    paintingVariables.nextEndTime = endTime

    //Paint the *current* value until the next start
    paintingVariables.currentEndX = startX
    paintingVariables.currentEndTime = endTime

    paintingVariables.activeTimeStamp = activeTimeStamp
  }

  /**
   * Returns true if the *relevant* values have changed.
   * If this method returns true, a new segment will be started.
   * If this method returns false, the current segment will continue
   */
  abstract fun relevantValuesHaveChanged(
    value1: Value1Type,
    value2: Value2Type,
    value3: Value3Type,
    value4: Value4Type,
  ): Boolean

  /**
   * Is called for each "new" segment
   */
  fun layoutSegment(paintingContext: LayerPaintingContext): @Window @MayBeNaN Double {
    val paintingVariables = paintingVariables()

    @MayBeNoValueOrPending val value1ToPaint = paintingVariables.currentValue1
    @MayBeNoValueOrPending val value2ToPaint = paintingVariables.currentValue2
    @MayBeNoValueOrPending val value3ToPaint = paintingVariables.currentValue3
    @MayBeNoValueOrPending val value4ToPaint = paintingVariables.currentValue4

    @Window val startX = paintingVariables.currentStartX
    @Window val endX = paintingVariables.currentEndX

    @Window val startTime = paintingVariables.currentStartTime
    @Window val endTime = paintingVariables.currentEndTime

    @ms @MayBeNaN val activeTimeStamp = paintingVariables.activeTimeStamp

    try {
      paintingVariables.storeSegment(startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)

      @MayBeNaN @Window val opticalCenter = layoutSegment(paintingContext, startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)
      if (paintingVariables.activeTimeStamp in startTime..endTime) {
        return opticalCenter //Only return if this is relevant for the active time stamp
      }

      return Double.NaN
    } finally {
      //Switch to *next*
      paintingVariables.prepareForNextValue()
    }
  }

  /**
   * Layouts the segment
   * @return the geometrical center
   */
  fun layoutSegment(
    paintingContext: LayerPaintingContext,
    startX: @Window Double,
    endX: @Window Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    value1ToPaint: Value1Type,
    value2ToPaint: Value2Type,
    value3ToPaint: Value3Type,
    value4ToPaint: Value4Type,
  ): @Window @MayBeNaN Double {
    paintingVariables().storeSegment(startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)
    return (startX + endX) / 2.0
  }

  override fun layoutFinish(paintingContext: LayerPaintingContext): @Window @MayBeNaN Double {
    //Paint the last value
    return layoutSegment(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val paintingVariables = paintingVariables()
    paintingVariables.verifyLoopIndex(paintingContext)

    beginPainting(paintingContext)

    paintingVariables.segments.fastForEachWithIndex { index, segmentLayoutVariables ->
      paintSegment(
        paintingContext,
        segmentLayoutVariables.startX,
        segmentLayoutVariables.endX,
        segmentLayoutVariables.activeTimeStamp,
        segmentLayoutVariables.value1ToPaint,
        segmentLayoutVariables.value2ToPaint,
        segmentLayoutVariables.value3ToPaint,
        segmentLayoutVariables.value4ToPaint,
      )
    }

    finishPainting(paintingContext)
  }

  open fun beginPainting(paintingContext: LayerPaintingContext) {
  }

  /**
   * Paints a single segment.
   * This method might be called multiple times.
   *
   * * [beginPainting] is called once before
   * * [finishPainting] is called once after
   */
  abstract fun paintSegment(
    paintingContext: LayerPaintingContext,
    startX: @Window Double, endX: @Window Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    value1ToPaint: Value1Type, value2ToPaint: Value2Type, value3ToPaint: Value3Type, value4ToPaint: Value4Type,
  )

  open fun finishPainting(paintingContext: LayerPaintingContext) {
  }
}
