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
import it.neckar.open.unit.number.MayBeNaN

/**
 * Base interface for stripe painters.
 * ATTENTION: Do *not* use one instance for multiple data series. The painting variables are cached!
 *
 * This painter has a (complex) layout phase.
 *
 * The relevant values of a value change are *not* part of this interface - they are concrete value classes and are
 * added by the concrete stripe painter interfaces (e.g. [com.meistercharts.algorithms.painter.stripe.enums.EnumStripePainter]).
 * This keeps the value classes off the generic seam and avoids boxing in the hot loop.
 *
 * ### Layout phase
 * * 1: [layoutBegin] is called once for each data series
 * * 0-n: `layoutValueChange` (declared by the concrete painter) is called for every value change
 * * 1: [layoutFinish] is called once for each data series
 *
 * ### Paining phase:
 * * 1: [paint]
 *
 * @param DataSeriesIndexType: Type of: the data series index
 */
interface StripePainter<DataSeriesIndexType : DataSeriesIndex> {
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
