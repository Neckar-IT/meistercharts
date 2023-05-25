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
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
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
   * Returns the painting variables for the provided data series index
   */
  protected fun forDataSeriesIndex(dataSeriesIndex: DataSeriesIndexType): StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type> {
    return paintingVariables().forDataSeriesIndex(dataSeriesIndex)
  }

  override fun layoutBegin(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    paintingVariables().prepareLayout(paintingContext, height, dataSeriesIndex, historyConfiguration)
  }

  override fun layoutValueChange(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DataSeriesIndexType,
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

    if (haveRelevantValuesChanged(dataSeriesIndex, newValue1, newValue2, newValue3, newValue4).not()) {
      //Values have not changed, just update the end - but do not paint
      paintingVariables.forDataSeriesIndex(dataSeriesIndex).updateCurrentEnd(endX, endTime)
      return Double.NaN
    }

    paintingVariables.forDataSeriesIndex(dataSeriesIndex).relevantValuesChanged(newValue1, newValue2, newValue3, newValue4, startX, endX, startTime, endTime, activeTimeStamp)

    return paintingVariables.forDataSeriesIndex(dataSeriesIndex).layoutSegment()
  }

  /**
   * Returns true if the *relevant* values have changed.
   * If this method returns true, a new segment will be started.
   * If this method returns false, the current segment will continue
   */
  abstract fun haveRelevantValuesChanged(
    dataSeriesIndex: DataSeriesIndexType,
    value1: Value1Type,
    value2: Value2Type,
    value3: Value3Type,
    value4: Value4Type,
  ): Boolean

  override fun layoutFinish(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType): @Window @MayBeNaN Double {
    //Paint the last value
    return paintingVariables().forDataSeriesIndex(dataSeriesIndex).layoutSegment()
  }

  override fun paint(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType) {
    val paintingVariables = paintingVariables()
    paintingVariables.verifyLoopIndex(paintingContext)

    beginPainting(paintingContext, dataSeriesIndex)

    //Use the painting variables for the data series
    paintingVariables.forDataSeriesIndex(dataSeriesIndex).let { paintingVariablesForDataSeries ->
      paintingVariablesForDataSeries.segments.fastForEachWithIndex { _, segmentLayoutVariables ->
        paintSegment(
          paintingContext,
          dataSeriesIndex,
          segmentLayoutVariables.startX,
          segmentLayoutVariables.endX,
          segmentLayoutVariables.activeTimeStamp,
          segmentLayoutVariables.value1ToPaint,
          segmentLayoutVariables.value2ToPaint,
          segmentLayoutVariables.value3ToPaint,
          segmentLayoutVariables.value4ToPaint,
        )
      }
    }

    finishPainting(paintingContext)
  }

  open fun beginPainting(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType) {
  }

  /**
   * Paints a single segment.
   * This method might be called multiple times - if there are multiple segments.
   *
   * * [beginPainting] is called once before
   * * [finishPainting] is called once after
   */
  abstract fun paintSegment(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DataSeriesIndexType,
    startX: @Window Double, endX: @Window Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    value1ToPaint: Value1Type, value2ToPaint: Value2Type, value3ToPaint: Value3Type, value4ToPaint: Value4Type,
  )

  open fun finishPainting(paintingContext: LayerPaintingContext) {
  }
}
