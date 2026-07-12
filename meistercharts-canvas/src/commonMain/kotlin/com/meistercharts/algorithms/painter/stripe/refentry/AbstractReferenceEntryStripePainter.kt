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
import com.meistercharts.algorithms.painter.stripe.AbstractStripePainter
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Abstract base class for referenceEntry stripe painters
 */
abstract class AbstractReferenceEntryStripePainter :
  AbstractStripePainter<ReferenceEntryDataSeriesIndex, ReferenceEntryStripePainterPaintingVariablesForOneDataSeries.Segment, ReferenceEntryStripePainterPaintingVariablesForOneDataSeries>(),
  ReferenceEntryStripePainter {
  /**
   * Provides the current aggregation mode
   */
  abstract val configuration: Configuration

  override fun paintingVariables(): ReferenceEntryStripePainterPaintingVariables {
    return paintingVariables
  }

  /**
   * The painting properties that are held
   */
  private val paintingVariables = ReferenceEntryStripePainterPaintingVariables()

  override fun layoutValueChange(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: ReferenceEntryDataSeriesIndex,
    startX: @Window Double,
    endX: @Window Double,
    startTime: @ms Double,
    endTime: @ms Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    id: @MayBeNoValueOrPending ReferenceEntryId,
    differentIdsCount: @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount,
    status: @MayBeNoValueOrPending HistoryEnumSet,
    data: ReferenceEntryData?,
  ): @Window @MayBeNaN Double {
    val paintingVariablesForDataSeries = forDataSeriesIndex(dataSeriesIndex)

    if (haveRelevantValuesChanged(paintingVariablesForDataSeries, id, differentIdsCount, status).not()) {
      //Values have not changed, just update the end - but do not paint
      paintingVariablesForDataSeries.updateCurrentEnd(endX, endTime)
      return Double.NaN
    }

    paintingVariablesForDataSeries.storeNextValues(id.id, differentIdsCount.value, status.bitset, data)
    paintingVariablesForDataSeries.recordValueChange(startX, endX, startTime, endTime, activeTimeStamp)
    return paintingVariablesForDataSeries.layoutSegment()
  }

  /**
   * Returns true if the *relevant* values have changed.
   * The reference entry data ([data]) is intentionally excluded - it must never change for the same [ReferenceEntryId].
   * Compares raw ints, no allocation.
   */
  private fun haveRelevantValuesChanged(
    paintingVariablesForDataSeries: ReferenceEntryStripePainterPaintingVariablesForOneDataSeries,
    newId: @MayBeNoValueOrPending ReferenceEntryId,
    newCount: @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount,
    newStatus: @MayBeNoValueOrPending HistoryEnumSet,
  ): Boolean {
    return paintingVariablesForDataSeries.currentId != newId.id ||
      paintingVariablesForDataSeries.currentCount != newCount.value ||
      paintingVariablesForDataSeries.currentStatus != newStatus.bitset
  }

  @ConfigurationDsl
  open class Configuration {
  }
}
