package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.painter.stripe.StripePainter
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryDifferentIdsCount


/**
 * Visualizes a reference entry value as horizontal bar with different styles - depending on the enum value
 */
typealias ReferenceEntryStripePainter = StripePainter<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, ReferenceEntryData?>
