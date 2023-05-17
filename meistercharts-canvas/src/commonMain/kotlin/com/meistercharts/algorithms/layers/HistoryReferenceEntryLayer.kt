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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.algorithms.painter.stripe.refentry.ReferenceEntryStripePainter
import com.meistercharts.annotations.ContentArea
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.provider.MultiProvider

/**
 * Paints the history reference entries
 */
class HistoryReferenceEntryLayer(
  configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractHistoryStripeLayer<ReferenceEntryDataSeriesIndexProvider, ReferenceEntryDataSeriesIndex, ReferenceEntryStripePainter, ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>(
  configuration.also(additionalConfiguration)
) {

  override fun paintingVariables(): HistoryReferenceEntryPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : AbstractHistoryStripeLayerPaintingVariables(), HistoryReferenceEntryPaintingVariables {
    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)
    }

    override fun dataSeriesIndexFromInt(indexAsInt: Int): ReferenceEntryDataSeriesIndex {
      return ReferenceEntryDataSeriesIndex(indexAsInt)
    }
  }

  override fun dataSeriesCount(): Int {
    return configuration.historyConfiguration().referenceEntryDataSeriesCount
  }

  override fun HistoryChunk.getValue1(visibleDataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex): ReferenceEntryId {
    return getReferenceEntryId(visibleDataSeriesIndex, timestampIndex)
  }

  override fun HistoryChunk.getValue2(visibleDataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex): ReferenceEntryDifferentIdsCount {
    return getReferenceEntryIdsCount(visibleDataSeriesIndex, timestampIndex)
  }

  override fun HistoryChunk.getValue3(visibleDataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex): HistoryEnumSet {
    return getReferenceEntryStatus(visibleDataSeriesIndex, timestampIndex)
  }

  override fun HistoryChunk.getValue4(visibleDataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex): ReferenceEntryData? {
    val referenceEntryId = getReferenceEntryId(visibleDataSeriesIndex, timestampIndex)
    return getReferenceEntryData(visibleDataSeriesIndex, referenceEntryId)
  }

  override fun value1Default(): ReferenceEntryId {
    return ReferenceEntryId.NoValue
  }

  override fun value2Default(): ReferenceEntryDifferentIdsCount {
    return ReferenceEntryDifferentIdsCount.NoValue
  }

  override fun value3Default(): HistoryEnumSet {
    return HistoryEnumSet.NoValue
  }

  override fun value4Default(): ReferenceEntryData? {
    return null
  }

  class Configuration(
    /**
     * Where the history is stored
     */
    historyStorage: HistoryStorage,

    historyConfiguration: () -> HistoryConfiguration,

    /**
     * The visible indices - must only include indices that *do* exist in the history configuration.
     * The [com.meistercharts.charts.refs.DiscreteTimelineChartGestalt] handles these cases.
     */
    requestedVisibleIndices: ReferenceEntryDataSeriesIndexProvider,

    /**
     * Provides the time range of the content area
     */
    contentAreaTimeRange: @ContentArea TimeRangeProvider,
  ) : AbstractHistoryStripeLayer.Configuration<ReferenceEntryDataSeriesIndexProvider, ReferenceEntryDataSeriesIndex, ReferenceEntryStripePainter, ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>(
    historyStorage = historyStorage,
    historyConfiguration = historyConfiguration,
    requestedVisibleIndices = requestedVisibleIndices,
    contentAreaTimeRange = contentAreaTimeRange,
    stripePainters = MultiProvider.always(RectangleReferenceEntryStripePainter())
  )

  interface HistoryReferenceEntryPaintingVariables : HistoryStripeLayerPaintingVariables<ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>
}


inline val AbstractHistoryStripeLayer.HistoryStripeLayerPaintingVariables.ActiveInformation<ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>.referenceEntryId: @MayBeNoValueOrPending ReferenceEntryId
  get() {
    return value1
  }

inline val AbstractHistoryStripeLayer.HistoryStripeLayerPaintingVariables.ActiveInformation<ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>.differentIdsCount: @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount
  get() {
    return value2
  }

inline val AbstractHistoryStripeLayer.HistoryStripeLayerPaintingVariables.ActiveInformation<ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>.status: @MayBeNoValueOrPending HistoryEnumSet
  get() {
    return value3
  }

inline val AbstractHistoryStripeLayer.HistoryStripeLayerPaintingVariables.ActiveInformation<ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>.referenceEntryData: ReferenceEntryData?
  get() {
    return value4
  }
