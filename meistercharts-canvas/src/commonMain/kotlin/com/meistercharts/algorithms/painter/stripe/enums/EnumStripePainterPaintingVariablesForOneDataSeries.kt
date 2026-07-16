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

import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariablesForOneDataSeries
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import it.neckar.open.annotations.Hot

/**
 * Painting variables for a single enum data series.
 *
 * The enum set and (most-time) ordinal are stored as raw ints ([HistoryEnumSetInt] / [HistoryEnumOrdinalInt]) to avoid
 * boxing the [HistoryEnumSet] / [HistoryEnumOrdinal] value classes in the timeline hot loop. They are wrapped into the
 * value classes only at the paint boundary.
 */
class EnumStripePainterPaintingVariablesForOneDataSeries :
  StripePainterPaintingVariablesForOneDataSeries<EnumDataSeriesIndex, EnumStripePainterPaintingVariablesForOneDataSeries.Segment>(EnumDataSeriesIndex.zero) {

  var currentEnumSet: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
  var currentOrdinal: @HistoryEnumOrdinalInt Int = HistoryEnumOrdinal.NoValue.value

  var previousEnumSet: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
  var previousOrdinal: @HistoryEnumOrdinalInt Int = HistoryEnumOrdinal.NoValue.value

  var nextEnumSet: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
  var nextOrdinal: @HistoryEnumOrdinalInt Int = HistoryEnumOrdinal.NoValue.value

  /**
   * Stores the next values (as raw ints)
   */
  @Hot
  fun storeNextValues(enumSet: @HistoryEnumSetInt Int, ordinal: @HistoryEnumOrdinalInt Int) {
    nextEnumSet = enumSet
    nextOrdinal = ordinal
  }

  override fun createSegment(): Segment {
    return Segment()
  }

  @Hot
  override fun writeCurrentValuesToSegment(segment: Segment) {
    segment.enumSet = currentEnumSet
    segment.ordinal = currentOrdinal
  }

  override fun resetCurrentAndPreviousValues() {
    currentEnumSet = HistoryEnumSet.NoValueAsInt
    currentOrdinal = HistoryEnumOrdinal.NoValue.value
    previousEnumSet = HistoryEnumSet.NoValueAsInt
    previousOrdinal = HistoryEnumOrdinal.NoValue.value
  }

  @Hot
  override fun moveNextValuesToCurrentAndCurrentToPrevious() {
    previousEnumSet = currentEnumSet
    previousOrdinal = currentOrdinal
    currentEnumSet = nextEnumSet
    currentOrdinal = nextOrdinal
  }

  @Hot
  override fun resetNextValues() {
    nextEnumSet = HistoryEnumSet.NoValueAsInt
    nextOrdinal = HistoryEnumOrdinal.NoValue.value
  }

  /**
   * The variables for a single enum segment.
   */
  class Segment : SegmentLayoutVariables() {
    var enumSet: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
    var ordinal: @HistoryEnumOrdinalInt Int = HistoryEnumOrdinal.NoValue.value

    override fun resetValues() {
      enumSet = HistoryEnumSet.NoValueAsInt
      ordinal = HistoryEnumOrdinal.NoValue.value
    }
  }
}
