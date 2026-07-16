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
package com.meistercharts.algorithms.painter.stripe.enums

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.stripe.StripePainter
import com.meistercharts.annotations.Window
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Visualizes an enum value as horizontal bar with different styles - depending on the enum value.
 *
 * The relevant values ([HistoryEnumSet] / [HistoryEnumOrdinal]) are concrete value classes here - not on a generic
 * seam - so no boxing happens when the layer feeds the values in the hot loop.
 */
interface EnumStripePainter : StripePainter<EnumDataSeriesIndex> {
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
    dataSeriesIndex: EnumDataSeriesIndex,
    startX: @Window Double,
    endX: @Window Double,
    startTime: @ms Double,
    endTime: @ms Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    /**
     * The updated enum set
     */
    enumSet: @MayBeNoValueOrPending HistoryEnumSet,
    /**
     * The updated "most of the time" ordinal
     */
    mostTimeOrdinal: @MayBeNoValueOrPending HistoryEnumOrdinal,
  ): @Window @MayBeNaN Double
}
