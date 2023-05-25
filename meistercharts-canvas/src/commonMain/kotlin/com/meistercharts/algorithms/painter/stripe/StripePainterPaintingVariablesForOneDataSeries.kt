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
package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.cache.LayoutVariable
import com.meistercharts.canvas.layout.cache.LayoutVariablesObjectCache
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Contains the painting variables for a single data series
 */
class StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type>
  (
  /**
   * Defaults value for data series index - will be set initially and on reset
   */
  val dataSeriesIndexDefault: DataSeriesIndexType,
  /**
   * The default value for value1* - will be set initially and on reset
   */
  val value1Default: Value1Type,
  /**
   * The default value for value2* - will be set initially and on reset
   */
  val value2Default: Value2Type,

  /**
   * The default value for value3 - will be set initially and on reset
   */
  val value3Default: Value3Type,

  /**
   * The default value for value4 - will be set initially and on reset
   */
  val value4Default: Value4Type,
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

  // Contains the previous values
  var previousValue1: Value1Type = value1Default
  var previousValue2: Value2Type = value2Default
  var previousValue3: Value3Type = value3Default
  var previousValue4: Value4Type = value4Default


  //Contains the current values - that will be painted, soon
  var currentValue1: Value1Type = value1Default
  var currentValue2: Value2Type = value2Default
  var currentValue3: Value3Type = value3Default
  var currentValue4: Value4Type = value4Default

  var currentStartX: @Window Double = Double.NaN
  var currentEndX: @Window Double = Double.NaN
  var currentStartTime: @Window Double = Double.NaN

  var currentEndTime: @Window Double = Double.NaN
  var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN


  //Contains the next values - that are painted in the next segment
  var nextValue1: @MayBeNoValueOrPending Value1Type = value1Default
  var nextValue2: @MayBeNoValueOrPending Value2Type = value2Default
  var nextValue3: @MayBeNoValueOrPending Value3Type = value3Default
  var nextValue4: @MayBeNoValueOrPending Value4Type = value4Default

  var nextStartX: @Window Double = Double.NaN
  var nextEndX: @Window Double = Double.NaN
  var nextStartTime: @Window Double = Double.NaN
  var nextEndTime: @Window Double = Double.NaN


  /**
   * Contains the information about the segments.
   * Each segment might have a different length (and span multiple data points)
   */
  val segments: LayoutVariablesObjectCache<SegmentLayoutVariables<Value1Type, Value2Type, Value3Type, Value4Type>> = LayoutVariablesObjectCache {
    SegmentLayoutVariables(value1Default, value2Default, value3Default, value4Default)
  }

  /**
   * Is called *once* in each painting loop
   */
  fun prepareLayout(height: @Zoomed Double, historyConfiguration: HistoryConfiguration, dataSeriesIndex: DataSeriesIndexType) {
    this.height = height
    this.historyConfiguration = historyConfiguration
    this.visibleDataSeriesIndex = dataSeriesIndex
  }

  fun relevantValuesChanged(newValue1: Value1Type, newValue2: Value2Type, newValue3: Value3Type, newValue4: Value4Type, startX: @Window Double, endX: @Window Double, startTime: @ms Double, endTime: @ms Double, activeTimeStamp: @ms @MayBeNaN Double) {
    //Remember the updated properties - for the next paint
    nextValue1 = newValue1
    nextValue2 = newValue2
    nextValue3 = newValue3
    nextValue4 = newValue4

    nextStartX = startX
    nextEndX = endX
    nextStartTime = startTime
    nextEndTime = endTime

    //Paint the *current* value until the next start
    currentEndX = startX
    currentEndTime = endTime

    this.activeTimeStamp = activeTimeStamp
  }

  /**
   * Is called every time a segment should be layouted.
   * This method will then prepare a new segment.
   */
  fun layoutSegment(): @Window Double {
    @MayBeNoValueOrPending val value1ToPaint = currentValue1
    @MayBeNoValueOrPending val value2ToPaint = currentValue2
    @MayBeNoValueOrPending val value3ToPaint = currentValue3
    @MayBeNoValueOrPending val value4ToPaint = currentValue4

    @Window val startX = currentStartX
    @Window val endX = currentEndX

    @Window val startTime = currentStartTime
    @Window val endTime = currentEndTime

    @ms @MayBeNaN val activeTimeStamp = activeTimeStamp

    try {
      layoutSegment(startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)

      @MayBeNaN @Window val opticalCenter = this.layoutSegment(startX, endX, activeTimeStamp, value1ToPaint, value2ToPaint, value3ToPaint, value4ToPaint)
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
   * Is called during the layout phase to store the next segment that will then be painted in the painting phase
   *
   * @return the geometrical center
   */
  fun layoutSegment(startX: @Window Double, endX: @Window Double, activeTimeStamp: @ms @MayBeNaN Double, value1ToPaint: Value1Type, value2ToPaint: Value2Type, value3ToPaint: Value3Type, value4ToPaint: Value4Type): @Window Double {
    val segmentLayoutVariable = segments.addNewElement()

    segmentLayoutVariable.startX = startX
    segmentLayoutVariable.endX = endX
    segmentLayoutVariable.activeTimeStamp = activeTimeStamp
    segmentLayoutVariable.value1ToPaint = value1ToPaint
    segmentLayoutVariable.value2ToPaint = value2ToPaint
    segmentLayoutVariable.value3ToPaint = value3ToPaint
    segmentLayoutVariable.value4ToPaint = value4ToPaint

    return (startX + endX) / 2.0
  }

  override fun reset() {
    historyConfiguration = HistoryConfiguration.empty
    height = Double.NaN

    segments.clear() //reset the size to 0, will be added if necessary

    visibleDataSeriesIndex = dataSeriesIndexDefault

    currentValue1 = value1Default
    currentValue2 = value2Default
    currentValue3 = value3Default
    currentValue4 = value4Default

    currentStartX = Double.NaN
    currentEndX = Double.NaN
    currentStartTime = Double.NaN
    currentEndTime = Double.NaN

    previousValue1 = value1Default
    previousValue2 = value2Default
    previousValue3 = value3Default
    previousValue4 = value4Default

    activeTimeStamp = Double.NaN

    resetNextValues()
  }

  /**
   * Prepares for the next value
   */
  fun prepareForNextValue() {
    //Save current to previous
    previousValue1 = currentValue1
    previousValue2 = currentValue2
    previousValue3 = currentValue3
    previousValue4 = currentValue4

    //Save next to current
    currentValue1 = nextValue1
    currentValue2 = nextValue2
    currentValue3 = nextValue3
    currentValue4 = nextValue4

    currentStartX = nextStartX
    currentEndX = nextEndX
    currentStartTime = nextStartTime
    currentEndTime = nextEndTime

    //Reset next
    resetNextValues()
  }

  /**
   * Resets the next values to their defaults
   */
  private fun resetNextValues() {
    //reset the next
    nextValue1 = value1Default
    nextValue2 = value2Default
    nextValue3 = value3Default
    nextValue4 = value4Default

    nextStartX = Double.NaN
    nextEndX = Double.NaN
    nextStartTime = Double.NaN
    nextEndTime = Double.NaN
  }

  /**
   * Updates the end of the current segment
   */
  fun updateCurrentEnd(endX: @Window Double, endTime: @ms Double) {
    currentEndX = endX
    currentEndTime = endTime
  }

  /**
   * Contains the variables for a single segment
   */
  class SegmentLayoutVariables<Value1Type, Value2Type, Value3Type, Value4Type>(
    val defaultValue1ToPaint: Value1Type,
    val defaultValue2ToPaint: Value2Type,
    val defaultValue3ToPaint: Value3Type,
    val defaultValue4ToPaint: Value4Type,

    ) : LayoutVariable {

    var startX: @Window Double = Double.NaN
    var endX: @Window Double = Double.NaN
    var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

    var value1ToPaint: Value1Type = defaultValue1ToPaint
    var value2ToPaint: Value2Type = defaultValue2ToPaint
    var value3ToPaint: Value3Type = defaultValue3ToPaint
    var value4ToPaint: Value4Type = defaultValue4ToPaint


    override fun reset() {
      startX = Double.NaN
      endX = Double.NaN
      activeTimeStamp = Double.NaN

      value1ToPaint = defaultValue1ToPaint
      value2ToPaint = defaultValue2ToPaint
      value3ToPaint = defaultValue3ToPaint
      value4ToPaint = defaultValue4ToPaint
    }
  }
}
