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
import com.meistercharts.algorithms.painter.stripe.AbstractStripePainter
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Abstract base class for enum stripe painters
 */
abstract class AbstractEnumStripePainter :
  AbstractStripePainter<EnumDataSeriesIndex, EnumStripePainterPaintingVariablesForOneDataSeries.Segment, EnumStripePainterPaintingVariablesForOneDataSeries>(),
  EnumStripePainter {
  /**
   * Provides the current aggregation mode
   */
  abstract val configuration: Configuration

  @Hot
  override fun paintingVariables(): EnumStripePainterPaintingVariables {
    return paintingVariables
  }

  /**
   * The painting properties that are held
   */
  private val paintingVariables = EnumStripePainterPaintingVariables()

  /**
   * Returns the history enum value for the given index
   */
  @Hot
  fun getHistoryEnum(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnum {
    return paintingVariables().historyConfiguration.enumConfiguration.getEnum(dataSeriesIndex)
  }

  @Hot
  override fun layoutValueChange(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: EnumDataSeriesIndex,
    startX: @Window Double,
    endX: @Window Double,
    startTime: @ms Double,
    endTime: @ms Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    enumSet: @MayBeNoValueOrPending HistoryEnumSet,
    mostTimeOrdinal: @MayBeNoValueOrPending HistoryEnumOrdinal,
  ): @Window @MayBeNaN Double {
    val paintingVariablesForDataSeries = forDataSeriesIndex(dataSeriesIndex)

    if (haveRelevantValuesChanged(paintingVariablesForDataSeries, enumSet, mostTimeOrdinal).not()) {
      //Values have not changed, just update the end - but do not paint
      paintingVariablesForDataSeries.updateCurrentEnd(endX, endTime)
      return Double.NaN
    }

    paintingVariablesForDataSeries.storeNextValues(enumSet.bitset, mostTimeOrdinal.value)
    paintingVariablesForDataSeries.recordValueChange(startX, endX, startTime, endTime, activeTimeStamp)
    return paintingVariablesForDataSeries.layoutSegment()
  }

  /**
   * Returns true if the *relevant* value has changed - depending on the aggregation mode.
   * Reads the current values as raw ints and wraps them into value classes only for the comparison (no allocation).
   */
  @Hot
  private fun haveRelevantValuesChanged(
    paintingVariablesForDataSeries: EnumStripePainterPaintingVariablesForOneDataSeries,
    newEnumSet: @MayBeNoValueOrPending HistoryEnumSet,
    newMostTimeOrdinal: @MayBeNoValueOrPending HistoryEnumOrdinal,
  ): Boolean {
    return when (configuration.aggregationMode) {
      EnumAggregationMode.ByOrdinal -> {
        @MayBeNoValueOrPending val currentEnumSet = HistoryEnumSet(paintingVariablesForDataSeries.currentEnumSet)
        //Check if the enum set is the same first
        if (currentEnumSet == newEnumSet) {
          //necessary for NoValue/Pending
          false
        } else {
          //Now check *only* the first ordinal
          currentEnumSet.firstSetOrdinal() != newEnumSet.firstSetOrdinal()
        }
      }

      EnumAggregationMode.MostTime -> {
        //Check if the ordinal most time is the same
        paintingVariablesForDataSeries.currentOrdinal != newMostTimeOrdinal.value
      }
    }
  }

  @ConfigurationDsl
  open class Configuration {
    /**
     * How the enums are displayed when down sampled
     */
    var aggregationMode: EnumAggregationMode = EnumAggregationMode.MostTime
  }
}
