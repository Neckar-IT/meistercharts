package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.painter.stripe.AbstractStripePainterPaintingVariables
import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariables
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryDifferentIdsCount

/**
 * Painting variables for enums
 */
interface ReferenceEntryStripePainterPaintingVariables : StripePainterPaintingVariables<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, ReferenceEntryData?> {
  //TODO add
}

/**
 * Painting variables for referenceEntry stripes
 */
class DefaultReferenceEntryStripePainterPaintingVariables : AbstractStripePainterPaintingVariables<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, ReferenceEntryData?>(
  dataSeriesIndexDefault = ReferenceEntryDataSeriesIndex.zero,
  value1Default = ReferenceEntryId.NoValue,
  value2Default = ReferenceEntryDifferentIdsCount.NoValue,
  value3Default = null,
), ReferenceEntryStripePainterPaintingVariables {
}
