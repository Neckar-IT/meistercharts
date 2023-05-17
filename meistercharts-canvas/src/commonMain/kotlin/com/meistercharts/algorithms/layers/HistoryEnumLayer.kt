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

import com.meistercharts.algorithms.painter.stripe.enums.EnumStripePainter
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.annotations.ContentArea
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.provider.MultiProvider

/**
 * Paints the history enumerations
 */
class HistoryEnumLayer(
  configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractHistoryStripeLayer<EnumDataSeriesIndexProvider, EnumDataSeriesIndex, EnumStripePainter, HistoryEnumSet, HistoryEnumOrdinal, Unit, Unit>(
  configuration.also(additionalConfiguration)
) {

  override fun paintingVariables(): HistoryEnumPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : AbstractHistoryStripeLayerPaintingVariables(), HistoryEnumPaintingVariables {
    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)
    }

    override fun dataSeriesIndexFromInt(indexAsInt: Int): EnumDataSeriesIndex {
      return EnumDataSeriesIndex(indexAsInt)
    }
  }

  override fun dataSeriesCount(): Int {
    return configuration.historyConfiguration().enumDataSeriesCount
  }

  override fun HistoryChunk.getValue1(visibleDataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex): HistoryEnumSet {
    return this.getEnumValue(visibleDataSeriesIndex, timestampIndex)
  }

  override fun HistoryChunk.getValue2(visibleDataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex): HistoryEnumOrdinal {
    return getEnumOrdinalMostTime(visibleDataSeriesIndex, timestampIndex)
  }

  override fun HistoryChunk.getValue3(visibleDataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex) {
    return //do nothing
  }

  override fun HistoryChunk.getValue4(visibleDataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex) {
    return //do nothing
  }

  override fun value1Default(): HistoryEnumSet {
    return HistoryEnumSet.NoValue
  }

  override fun value2Default(): HistoryEnumOrdinal {
    return HistoryEnumOrdinal.NoValue
  }

  override fun value3Default() {
    //do nothing
  }

  override fun value4Default() {
    //do nothing
  }

  class Configuration(
    /**
     * Where the history is stored
     */
    historyStorage: HistoryStorage,

    historyConfiguration: () -> HistoryConfiguration,

    /**
     * The visible indices
     *
     * ATTENTION: This might contain indices that do *not* exist.
     * Therefore, it is necessary to check whether the data series for the given index does exist
     */
    requestedVisibleIndices: EnumDataSeriesIndexProvider,

    /**
     * Provides the time range of the content area
     */
    contentAreaTimeRange: @ContentArea TimeRangeProvider,
  ) : AbstractHistoryStripeLayer.Configuration<EnumDataSeriesIndexProvider, EnumDataSeriesIndex, EnumStripePainter, HistoryEnumSet, HistoryEnumOrdinal, Unit, Unit>(
    historyStorage = historyStorage,
    historyConfiguration = historyConfiguration,
    requestedVisibleIndices = requestedVisibleIndices,
    contentAreaTimeRange = contentAreaTimeRange,
    stripePainters = MultiProvider.always(RectangleEnumStripePainter()),
  )
}

interface HistoryEnumPaintingVariables : AbstractHistoryStripeLayer.HistoryStripeLayerPaintingVariables<HistoryEnumSet, HistoryEnumOrdinal, Unit, Unit> {
}
