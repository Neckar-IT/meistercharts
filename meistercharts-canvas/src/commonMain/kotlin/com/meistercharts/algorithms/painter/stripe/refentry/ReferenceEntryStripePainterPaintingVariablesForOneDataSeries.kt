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

import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariablesForOneDataSeries
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryDifferentIdsCountInt
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import it.neckar.open.annotations.Hot

/**
 * Painting variables for a single reference-entry data series.
 *
 * The id, different-ids count and status are stored as raw ints to avoid boxing the [ReferenceEntryId] /
 * [ReferenceEntryDifferentIdsCount] / [HistoryEnumSet] value classes in the timeline hot loop. The reference-entry data
 * ([ReferenceEntryData]) is a genuine object and is stored by reference.
 */
class ReferenceEntryStripePainterPaintingVariablesForOneDataSeries :
  StripePainterPaintingVariablesForOneDataSeries<ReferenceEntryDataSeriesIndex, ReferenceEntryStripePainterPaintingVariablesForOneDataSeries.Segment>(ReferenceEntryDataSeriesIndex.zero) {

  var currentId: @ReferenceEntryIdInt Int = ReferenceEntryId.NoValueAsInt
  var currentCount: @ReferenceEntryDifferentIdsCountInt Int = ReferenceEntryDifferentIdsCount.NoValueAsInt
  var currentStatus: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
  var currentData: ReferenceEntryData? = null

  var previousId: @ReferenceEntryIdInt Int = ReferenceEntryId.NoValueAsInt
  var previousCount: @ReferenceEntryDifferentIdsCountInt Int = ReferenceEntryDifferentIdsCount.NoValueAsInt
  var previousStatus: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
  var previousData: ReferenceEntryData? = null

  var nextId: @ReferenceEntryIdInt Int = ReferenceEntryId.NoValueAsInt
  var nextCount: @ReferenceEntryDifferentIdsCountInt Int = ReferenceEntryDifferentIdsCount.NoValueAsInt
  var nextStatus: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
  var nextData: ReferenceEntryData? = null

  /**
   * Stores the next values (id/count/status as raw ints, data by reference)
   */
  @Hot
  fun storeNextValues(id: @ReferenceEntryIdInt Int, count: @ReferenceEntryDifferentIdsCountInt Int, status: @HistoryEnumSetInt Int, data: ReferenceEntryData?) {
    nextId = id
    nextCount = count
    nextStatus = status
    nextData = data
  }

  override fun createSegment(): Segment {
    return Segment()
  }

  @Hot
  override fun writeCurrentValuesToSegment(segment: Segment) {
    segment.id = currentId
    segment.count = currentCount
    segment.status = currentStatus
    segment.data = currentData
  }

  override fun resetCurrentAndPreviousValues() {
    currentId = ReferenceEntryId.NoValueAsInt
    currentCount = ReferenceEntryDifferentIdsCount.NoValueAsInt
    currentStatus = HistoryEnumSet.NoValueAsInt
    currentData = null
    previousId = ReferenceEntryId.NoValueAsInt
    previousCount = ReferenceEntryDifferentIdsCount.NoValueAsInt
    previousStatus = HistoryEnumSet.NoValueAsInt
    previousData = null
  }

  @Hot
  override fun moveNextValuesToCurrentAndCurrentToPrevious() {
    previousId = currentId
    previousCount = currentCount
    previousStatus = currentStatus
    previousData = currentData
    currentId = nextId
    currentCount = nextCount
    currentStatus = nextStatus
    currentData = nextData
  }

  @Hot
  override fun resetNextValues() {
    nextId = ReferenceEntryId.NoValueAsInt
    nextCount = ReferenceEntryDifferentIdsCount.NoValueAsInt
    nextStatus = HistoryEnumSet.NoValueAsInt
    nextData = null
  }

  /**
   * The variables for a single reference-entry segment.
   */
  class Segment : SegmentLayoutVariables() {
    var id: @ReferenceEntryIdInt Int = ReferenceEntryId.NoValueAsInt
    var count: @ReferenceEntryDifferentIdsCountInt Int = ReferenceEntryDifferentIdsCount.NoValueAsInt
    var status: @HistoryEnumSetInt Int = HistoryEnumSet.NoValueAsInt
    var data: ReferenceEntryData? = null

    override fun resetValues() {
      id = ReferenceEntryId.NoValueAsInt
      count = ReferenceEntryDifferentIdsCount.NoValueAsInt
      status = HistoryEnumSet.NoValueAsInt
      data = null
    }
  }
}
