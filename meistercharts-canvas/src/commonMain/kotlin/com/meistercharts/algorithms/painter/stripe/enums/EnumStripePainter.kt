package com.meistercharts.algorithms.painter.stripe.enums

import com.meistercharts.algorithms.painter.stripe.StripePainter
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet


/**
 * Visualizes an enum value as horizontal bar with different styles - depending on the enum value
 */
typealias EnumStripePainter = StripePainter<EnumDataSeriesIndex, HistoryEnumSet, HistoryEnumOrdinal, Unit>
