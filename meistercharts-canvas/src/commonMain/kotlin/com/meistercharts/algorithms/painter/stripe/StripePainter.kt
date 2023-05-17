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
 * Base interface for stripe painters.
 * ATTENTION: Do *not* use one instance for multiple data series. The painting variables are cached!
 *
 * This painter has a (complex) layout phase.
 *
 *
 * ### Layout phase
 * * 1: [layoutBegin] is called once for each data series
 * * 0-n: [layoutValueChange] is called for every value change
 * * 1: [layoutFinish] is called once for each data series
 *
 * ### Paining phase:
 * * 1: [paint]
 *
 * @param DataSeriesIndexType: Type of: the data series index
 * @param ValueType1: Type of: the first relevant value
 * @param ValueType2: Type of: the second relevant value
 * @param ValueType4: Type of: the third relevant value
 */
interface StripePainter<DataSeriesIndexType : DataSeriesIndex, ValueType1, ValueType2, ValueType3, ValueType4> {
  /**
   * Is called when beginning to lay out a data series
   */
  fun layoutBegin(
    paintingContext: LayerPaintingContext,
    /**
     * The height of the stripe
     */
    height: @Zoomed Double,
    /**
     * The data series index the stripe belongs to
     */
    dataSeriesIndex: DataSeriesIndexType,
    /**
     * The history enum that is started to be painted
     */
    historyConfiguration: HistoryConfiguration,
  )


  /**
   * Adds a value change event at the given x location
   *
   * Call [layoutFinish] when done.
   *
   * @return the optical *center* of the segment - if the activeTimeStamp is within the segment. The center can be used for tooltips or other purposes.
   * Will return [Double.NaN] if `activeTimeStamp` is [Double.NaN] or outside the current segment.
   */
  fun layoutValueChange(
    paintingContext: LayerPaintingContext,
    /**
     * The data series index that is currently layouted
     */
    dataSeriesIndex: DataSeriesIndexType,

    /**
     * The start location of the stripe segment
     */
    startX: @Window Double,

    /**
     * The end location of the stripe segment
     */
    endX: @Window Double,

    startTime: @ms Double,
    endTime: @ms Double,

    /**
     * The active timestamp - is [Double.NaN] if there is no active timestamp
     */
    activeTimeStamp: @ms @MayBeNaN Double,

    /**
     * The updated value
     */
    newValue1: ValueType1,

    /**
     * The second updated value (usually some kind of merged type - e.g. "most of the time")
     */
    newValue2: ValueType2,

    /**
     * The third updated value
     */
    newValue3: ValueType3,

    /**
     * The fourth updated value (usually some kind of context information - e.g. a map with additional information required to paint)
     */
    newValue4: ValueType4,
  ): @Window @MayBeNaN Double


  /**
   * Is called at the end of the stripe
   */
  fun layoutFinish(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DataSeriesIndexType,
  ): @Window @MayBeNaN Double


  /**
   * Paints the data series.
   * Is called exactly once for each data series
   */
  fun paint(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DataSeriesIndexType,
  )
}
