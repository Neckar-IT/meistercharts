package com.meistercharts.algorithms.painter.stripe.enums

import com.meistercharts.algorithms.painter.stripe.AbstractStripePainter
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending

/**
 * Abstract base class for enum stripe painters
 */
abstract class AbstractEnumStripePainter : AbstractStripePainter<EnumDataSeriesIndex, @MayBeNoValueOrPending HistoryEnumSet, @MayBeNoValueOrPending HistoryEnumOrdinal, Unit>(), EnumStripePainter {
  /**
   * Provides the current aggregation mode
   */
  abstract val configuration: Configuration

  override fun paintingVariables(): EnumStripePainterPaintingVariables {
    return paintingVariables
  }

  /**
   * The painting properties that are held
   */
  private val paintingVariables = DefaultEnumStripePainterPaintingVariables()

  /**
   * Returns the history enum value for the given index
   */
  fun getHistoryEnum(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnum {
    val historyConfiguration = requireNotNull(paintingVariables().historyConfiguration) { "historyConfiguration not yet set" }
    return historyConfiguration.enumConfiguration.getEnum(dataSeriesIndex)
  }

  /**
   * Returns true if the relevant value has changed - depending on the
   */
  override fun relevantValuesHaveChanged(value1: @MayBeNoValueOrPending HistoryEnumSet, value2: @MayBeNoValueOrPending HistoryEnumOrdinal, value3: Unit): Boolean {
    @MayBeNoValueOrPending val currentEnumSet = paintingVariables.currentValue1
    @MayBeNoValueOrPending val currentEnumOrdinalMostTime = paintingVariables.currentValue2

    return when (configuration.aggregationMode) {
      EnumAggregationMode.ByOrdinal -> {
        //Check if the enum set is the same first
        if (currentEnumSet == value1) {
          //necessary for NoValue/Pending
          false
        } else {
          //Now check *only* the first ordinal
          currentEnumSet.firstSetOrdinal() != value1.firstSetOrdinal()
        }
      }

      EnumAggregationMode.MostTime -> {
        //Check if the ordinal most time is the same
        currentEnumOrdinalMostTime != value2
      }
    }
  }

  open class Configuration {
    /**
     * How the enums are displayed when down sampled
     */
    var aggregationMode: EnumAggregationMode = EnumAggregationMode.MostTime
  }
}
