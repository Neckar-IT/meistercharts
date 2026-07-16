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
package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.stripe.StripePainter
import com.meistercharts.annotations.Window
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Visualizes a reference entry value as horizontal bar with different styles - depending on the value.
 *
 * The relevant values ([ReferenceEntryId] / [ReferenceEntryDifferentIdsCount] / [HistoryEnumSet] / [ReferenceEntryData])
 * are concrete types here - not on a generic seam - so no boxing happens when the layer feeds the values in the hot loop.
 */
interface ReferenceEntryStripePainter : StripePainter<ReferenceEntryDataSeriesIndex> {
  /**
   * Adds a value change event at the given x location.
   *
   * Call [layoutFinish] when done.
   *
   * @return the optical *center* of the segment - if the activeTimeStamp is within the segment, [Double.NaN] otherwise.
   */
  @Hot
  fun layoutValueChange(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: ReferenceEntryDataSeriesIndex,
    startX: @Window Double,
    endX: @Window Double,
    startTime: @ms Double,
    endTime: @ms Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    /**
     * The updated reference entry id
     */
    id: @MayBeNoValueOrPending ReferenceEntryId,
    /**
     * The number of different ids (for down sampled data)
     */
    differentIdsCount: @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount,
    /**
     * The reference entry status
     */
    status: @MayBeNoValueOrPending HistoryEnumSet,
    /**
     * The reference entry data - context information required to paint. Must never change for the same [id].
     */
    data: ReferenceEntryData?,
  ): @Window @MayBeNaN Double
}
