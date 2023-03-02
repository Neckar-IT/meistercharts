package com.meistercharts.algorithms.painter.stripe.enums

import com.meistercharts.algorithms.painter.stripe.AbstractStripePainterPaintingVariables
import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariables
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet

/**
 * Painting variables for enums
 */
interface EnumStripePainterPaintingVariables : StripePainterPaintingVariables<EnumDataSeriesIndex, HistoryEnumSet, HistoryEnumOrdinal, Unit> {
  /**
   * The current history enum for [visibleDataSeriesIndex]
   */
  val historyEnum: HistoryEnum
}

/**
 * Painting variables for enum stripes
 */
class DefaultEnumStripePainterPaintingVariables : AbstractStripePainterPaintingVariables<EnumDataSeriesIndex, HistoryEnumSet, HistoryEnumOrdinal, Unit>(
  dataSeriesIndexDefault = EnumDataSeriesIndex.zero,
  value1Default = HistoryEnumSet.NoValue,
  value2Default = HistoryEnumOrdinal.NoValue,
  value3Default = Unit,
), EnumStripePainterPaintingVariables {
  /**
   * The current history enum for [visibleDataSeriesIndex]
   */
  override var historyEnum: HistoryEnum = HistoryEnum.Boolean
    private set

  override fun calculate(height: Double, dataSeriesIndex: EnumDataSeriesIndex, historyConfiguration: HistoryConfiguration) {
    super.calculate(height, dataSeriesIndex, historyConfiguration)
    historyEnum = getHistoryEnum(dataSeriesIndex)
  }

  /**
   * Returns the history enum value for the given index
   */
  fun getHistoryEnum(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnum {
    return this.historyConfiguration.enumConfiguration.getEnum(dataSeriesIndex)
  }
}
