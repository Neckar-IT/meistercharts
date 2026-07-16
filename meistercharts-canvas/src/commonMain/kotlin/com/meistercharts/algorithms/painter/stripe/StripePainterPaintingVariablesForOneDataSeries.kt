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
package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.buffer.LayoutVariable
import com.meistercharts.canvas.layout.buffer.LayoutVariablesObjectBuffer
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Contains the painting variables for a single data series.
 *
 * The concrete relevant values (enum set / ordinal, reference-entry id / count / status / data) are held by the
 * subclasses as raw primitives / references - not as boxed value classes. This avoids boxing in the timeline hot loop.
 * Only the (value-class agnostic) x/time state and the segment bookkeeping live here.
 */
abstract class StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType : DataSeriesIndex, SegmentType : StripePainterPaintingVariablesForOneDataSeries.SegmentLayoutVariables>(
  /**
   * Defaults value for data series index - will be set initially and on reset
   */
  val dataSeriesIndexDefault: DataSeriesIndexType,
) : LayoutVariable {

  /**
   * The current history configuration
   */
  var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty

  /**
   * The height of the data series
   */
  var height: @Zoomed Double = Double.NaN

  /**
   * The data series index of the visible data series
   */
  var visibleDataSeriesIndex: DataSeriesIndexType = dataSeriesIndexDefault

  var currentStartX: @Window Double = Double.NaN
  var currentEndX: @Window Double = Double.NaN
  var currentStartTime: @Window Double = Double.NaN

  var currentEndTime: @Window Double = Double.NaN
  var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

  var nextStartX: @Window Double = Double.NaN
  var nextEndX: @Window Double = Double.NaN
  var nextStartTime: @Window Double = Double.NaN
  var nextEndTime: @Window Double = Double.NaN

  /**
   * Contains the information about the segments.
   * Each segment might have a different length (and span multiple data points).
   *
   * The segment objects are reused across painting loops (object pool). The concrete subclass stores the per-segment
   * values as raw primitives / references in [SegmentType].
   */
  val segments: LayoutVariablesObjectBuffer<SegmentType> = LayoutVariablesObjectBuffer { createSegment() }

  /**
   * Creates a new (empty) segment. Called once per pool slot; the created objects are reused.
   */
  protected abstract fun createSegment(): SegmentType

  /**
   * Is called *once* in each painting loop
   */
  @Hot
  fun prepareLayout(height: @Zoomed Double, historyConfiguration: HistoryConfiguration, dataSeriesIndex: DataSeriesIndexType) {
    this.height = height
    this.historyConfiguration = historyConfiguration
    this.visibleDataSeriesIndex = dataSeriesIndex
  }

  /**
   * Remembers the x/time state of a value change. The subclass stores the concrete next values itself before/after
   * calling this method.
   */
  @Hot
  fun recordValueChange(startX: @Window Double, endX: @Window Double, startTime: @ms Double, endTime: @ms Double, activeTimeStamp: @ms @MayBeNaN Double) {
    nextStartX = startX
    nextEndX = endX
    nextStartTime = startTime
    nextEndTime = endTime

    //Paint the *current* value until the next start
    currentEndX = startX
    currentEndTime = startTime

    this.activeTimeStamp = activeTimeStamp
  }

  /**
   * Is called every time a segment should be layouted.
   * Appends a new segment for the *current* values and prepares for the next value.
   *
   * @return the geometrical center - if the active timestamp is within the segment, [Double.NaN] otherwise.
   */
  @Hot
  fun layoutSegment(): @Window @MayBeNaN Double {
    @Window val startX = currentStartX
    @Window val endX = currentEndX
    @Window val startTime = currentStartTime
    @Window val endTime = currentEndTime
    @ms @MayBeNaN val activeTimeStamp = activeTimeStamp

    @HotAllocation("Object pool - allocates only when the segment pool grows beyond its high-water mark; steady-state frames reuse pooled segments")
    val segment = segments.addNewElement()
    segment.startX = startX
    segment.endX = endX
    segment.activeTimeStamp = activeTimeStamp
    writeCurrentValuesToSegment(segment)

    try {
      @MayBeNaN @Window val opticalCenter = (startX + endX) / 2.0
      if (activeTimeStamp in startTime..endTime) {
        return opticalCenter //Only return if this is relevant for the active time stamp
      }

      return Double.NaN
    } finally {
      //Switch to *next*
      prepareForNextValue()
    }
  }

  /**
   * Writes the *current* values into the given segment.
   */
  @Hot
  protected abstract fun writeCurrentValuesToSegment(segment: SegmentType)

  override fun reset() {
    historyConfiguration = HistoryConfiguration.empty
    height = Double.NaN

    segments.clear() //reset the size to 0, will be added if necessary

    visibleDataSeriesIndex = dataSeriesIndexDefault

    currentStartX = Double.NaN
    currentEndX = Double.NaN
    currentStartTime = Double.NaN
    currentEndTime = Double.NaN

    activeTimeStamp = Double.NaN

    resetCurrentAndPreviousValues()
    resetNextValues()
    resetNextXTime()
  }

  /**
   * Resets the current and previous values to their defaults.
   */
  protected abstract fun resetCurrentAndPreviousValues()

  /**
   * Prepares for the next value
   */
  @Hot
  fun prepareForNextValue() {
    //Save current to previous, next to current
    moveNextValuesToCurrentAndCurrentToPrevious()

    currentStartX = nextStartX
    currentEndX = nextEndX
    currentStartTime = nextStartTime
    currentEndTime = nextEndTime

    //Reset next
    resetNextValues()
    resetNextXTime()
  }

  /**
   * Copies the current values to previous and the next values to current.
   */
  @Hot
  protected abstract fun moveNextValuesToCurrentAndCurrentToPrevious()

  /**
   * Resets the next values to their defaults.
   */
  @Hot
  protected abstract fun resetNextValues()

  @Hot
  private fun resetNextXTime() {
    nextStartX = Double.NaN
    nextEndX = Double.NaN
    nextStartTime = Double.NaN
    nextEndTime = Double.NaN
  }

  /**
   * Updates the end of the current segment
   */
  @Hot
  fun updateCurrentEnd(endX: @Window Double, endTime: @ms Double) {
    currentEndX = endX
    currentEndTime = endTime
  }

  /**
   * Contains the (value class agnostic) variables for a single segment.
   * Subclasses add the concrete per-segment values as raw primitives / references.
   */
  abstract class SegmentLayoutVariables : LayoutVariable {
    var startX: @Window Double = Double.NaN
    var endX: @Window Double = Double.NaN
    var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

    final override fun reset() {
      startX = Double.NaN
      endX = Double.NaN
      activeTimeStamp = Double.NaN

      resetValues()
    }

    /**
     * Resets the concrete per-segment values to their defaults.
     */
    protected abstract fun resetValues()
  }
}
