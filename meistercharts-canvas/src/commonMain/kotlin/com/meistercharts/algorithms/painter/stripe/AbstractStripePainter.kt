/*
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
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.number.MayBeNaN

/**
 * Abstract base class for stripe painters.
 *
 * A stripe painter paints a (horizontal) stripe with several stripe segments.
 * A segment might span multiple "value segments". This allows optimized painting of a single area for multiple recordings of the same value.
 *
 * The concrete relevant values are handled by the subclasses (raw primitives / references) - not on the generic seam -
 * to avoid boxing in the timeline hot loop. The subclass declares its own `layoutValueChange` with concrete value classes
 * and stores the values into [PaintingVariablesForOneDataSeriesType] via [recordValueChange] + [layoutSegment].
 */
abstract class AbstractStripePainter<
  DataSeriesIndexType : DataSeriesIndex,
  SegmentType : StripePainterPaintingVariablesForOneDataSeries.SegmentLayoutVariables,
  PaintingVariablesForOneDataSeriesType : StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType, SegmentType>,
  > : StripePainter<DataSeriesIndexType> {
  /**
   * Returns the painting variables for this stripe painter
   */
  @Hot
  abstract fun paintingVariables(): StripePainterPaintingVariables<DataSeriesIndexType, PaintingVariablesForOneDataSeriesType>

  /**
   * Returns the painting variables for the provided data series index
   */
  @Hot
  protected fun forDataSeriesIndex(dataSeriesIndex: DataSeriesIndexType): PaintingVariablesForOneDataSeriesType {
    return paintingVariables().forDataSeriesIndex(dataSeriesIndex)
  }

  @Hot
  override fun layoutBegin(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    paintingVariables().prepareLayout(paintingContext, height, dataSeriesIndex, historyConfiguration)
  }

  @Hot
  override fun layoutFinish(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType): @Window @MayBeNaN Double {
    //Paint the last value
    return forDataSeriesIndex(dataSeriesIndex).layoutSegment()
  }

  @Hot
  override fun paint(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType) {
    val paintingVariables = paintingVariables()
    paintingVariables.verifyLoopIndex(paintingContext)

    beginPainting(paintingContext, dataSeriesIndex)

    //Use the painting variables for the data series
    paintingVariables.forDataSeriesIndex(dataSeriesIndex).segments.fastForEach { segment ->
      paintSegment(paintingContext, dataSeriesIndex, segment)
    }

    finishPainting(paintingContext)
  }

  @Hot
  open fun beginPainting(paintingContext: LayerPaintingContext, dataSeriesIndex: DataSeriesIndexType) {
  }

  /**
   * Paints a single segment.
   * This method might be called multiple times - if there are multiple segments.
   *
   * The concrete per-segment values are read from [segment] as raw primitives / references and wrapped into value
   * classes only here, at the paint boundary.
   *
   * * [beginPainting] is called once before
   * * [finishPainting] is called once after
   */
  @Hot
  abstract fun paintSegment(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: DataSeriesIndexType,
    segment: SegmentType,
  )

  @Hot
  open fun finishPainting(paintingContext: LayerPaintingContext) {
  }
}
