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
import com.meistercharts.algorithms.layers.LoopIndexAware
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.PaintingLoopIndex
import com.meistercharts.canvas.layout.cache.MappedLayoutCache
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Painting variables for [StripePainter]s.
 *
 * This painting variables supports multiple data series by using [MappedLayoutCache] with the data series index as key
 */
abstract class StripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type>(
  /**
   * Defaults value for data series index - will be set initially and on reset
   */
  val dataSeriesIndexDefault: DataSeriesIndexType,
  /**
   * The default value for value1* - will be set initially and on reset
   */
  val value1Default: Value1Type,
  /**
   * The default value for value2* - will be set initially and on reset
   */
  val value2Default: Value2Type,

  /**
   * The default value for value3 - will be set initially and on reset
   */
  val value3Default: Value3Type,

  /**
   * The default value for value4 - will be set initially and on reset
   */
  val value4Default: Value4Type,

  ) : LoopIndexAware {

  /**
   * Contains the delegate painting variables for each data series
   */
  private val paintingVariables4DataSeries = MappedLayoutCache<DataSeriesIndexType, StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type>> {
    StripePainterPaintingVariablesForOneDataSeries(dataSeriesIndexDefault, value1Default, value2Default, value3Default, value4Default)
  }

  /**
   * Returns the painting variables for the provided data series index
   */
  fun getPaintingVariables(dataSeriesIndex: DataSeriesIndexType): StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type> {
    return paintingVariables4DataSeries.get(dataSeriesIndex)
  }

  override var loopIndex: PaintingLoopIndex = PaintingLoopIndex.Unknown

  /**
   * The current history configuration
   */
  var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty

  @Deprecated("Check if this is necessary - or could be replaced by parameters")
  var currentDataSeriesIndex: DataSeriesIndexType = dataSeriesIndexDefault

  /**
   * This method is called once for *each* data series index.
   * Therefore, if there are multiple data series shown, this method is called multiple times per loop.
   */
  open fun prepareLayout(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    reset(paintingContext.loopIndex)
    this.historyConfiguration = historyConfiguration
    this.currentDataSeriesIndex = dataSeriesIndex

    getPaintingVariables(dataSeriesIndex).prepareLayout(height, historyConfiguration, dataSeriesIndex)
  }

  /**
   * Updates *only* the end values. This method is called if no relevant values have changed.
   */
  fun updateCurrentEnd(endX: @Window Double, endTime: @ms Double) {
    getPaintingVariables(currentDataSeriesIndex).let { paintingVariables ->
      paintingVariables.currentEndX = endX
      paintingVariables.currentEndTime = endTime
    }
  }

  /**
   * Reset all values
   */
  fun reset(loopIndex: PaintingLoopIndex) {
    this.loopIndex = loopIndex
    this.currentDataSeriesIndex = dataSeriesIndexDefault
    this.historyConfiguration = HistoryConfiguration.empty
    paintingVariables4DataSeries.resetIfNewLoopIndex(loopIndex)
  }

  /**
   * Is called if the relevant values have changed
   */
  fun relevantValuesChanged(
    dataSeriesIndex: DataSeriesIndexType,
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

    getPaintingVariables(dataSeriesIndex).let { paintingVariables ->
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
  }

  /**
   * Is called for each "new" segment.
   *
   * @return the optical center if this segment itself is active
   */
  fun layoutSegment(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType): @Window @MayBeNaN Double {
    getPaintingVariables(dataSeriesIndex).let { paintingVariables ->

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

        @MayBeNaN @Window val opticalCenter = layoutSegment(paintingContext, dataSeriesIndex, startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)
        if (paintingVariables.activeTimeStamp in startTime..endTime) {
          return opticalCenter //Only return if this is relevant for the active time stamp
        }

        return Double.NaN
      } finally {
        //Switch to *next*
        paintingVariables.prepareForNextValue()
      }
    }
  }

  /**
   * Layouts the segment
   * @return the geometrical center
   */
  fun layoutSegment(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DataSeriesIndexType,
    startX: @Window Double,
    endX: @Window Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    value1ToPaint: Value1Type,
    value2ToPaint: Value2Type,
    value3ToPaint: Value3Type,
    value4ToPaint: Value4Type,
  ): @Window @MayBeNaN Double {
    getPaintingVariables(dataSeriesIndex).let { paintingVariables ->
      paintingVariables.storeSegment(startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)
      return (startX + endX) / 2.0
    }
  }

}

