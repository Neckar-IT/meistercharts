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
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Paining variables for stripe painters
 */
interface StripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type> {
  /**
   * Calculates the values. This method will be called initially before/when the painter is used.
   */
  fun calculate(height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration)

  /**
   * Prepares for the next value.
   *
   * This method might be called multiple times for each painter.
   * This method must only be called after [calculate] has been called once.
   */
  fun prepareForNextValue()

  /**
   * The height of the stripe
   */
  val height: @Zoomed Double

  /**
   * The history configuration - will be set delayed
   */
  val historyConfiguration: HistoryConfiguration

  /**
   * The data series index that is currently painted.
   * The index can be used to resolve other values (e.g. colors, fonts, labels)
   */
  val visibleDataSeriesIndex: DataSeriesIndexType


  /**
   * The value1 that will be painted.
   */
  val currentValue1: @MayBeNoValueOrPending Value1Type

  /**
   * The value2 that will be painted.
   */
  val currentValue2: @MayBeNoValueOrPending Value2Type

  /**
   * The value3 that will be painted.
   */
  val currentValue3: @MayBeNoValueOrPending Value3Type

  val currentValue4: @MayBeNoValueOrPending Value4Type


  /**
   * The value1 that has been painted before the current value
   */
  val previousValue1: @MayBeNoValueOrPending Value1Type

  /**
   * The value2 that has been painted before the current value
   */
  val previousValue2: @MayBeNoValueOrPending Value2Type

  /**
   * The value3 that has been painted before the current value
   */
  val previousValue3: @MayBeNoValueOrPending Value3Type

  val previousValue4: @MayBeNoValueOrPending Value4Type


  /**
   * The start location of the current stripe segment
   */
  val currentStartX: @Window Double

  /**
   * The end of the current segment.
   * This allows optimized painting (single rect of one color instead of multiple rects with the same color)
   */
  var currentEndX: @Window Double

  val currentStartTime: @ms Double
  var currentEndTime: @ms Double

  /**
   * The active timestamp - if there is one
   */
  var activeTimeStamp: @ms @MayBeNaN Double

  var nextValue1: @MayBeNoValueOrPending Value1Type
  var nextValue2: @MayBeNoValueOrPending Value2Type
  var nextValue3: @MayBeNoValueOrPending Value3Type
  var nextValue4: @MayBeNoValueOrPending Value4Type

  var nextStartX: @Window Double
  var nextEndX: @Window Double
  var nextStartTime: @ms Double
  var nextEndTime: @ms Double
}


/**
 * Abstract base class
 */
abstract class AbstractStripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type>(
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

  ) : StripePainterPaintingVariables<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type> {

  override var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty
    protected set

  override var height: Double = Double.NaN
    protected set

  override var visibleDataSeriesIndex: DataSeriesIndexType = dataSeriesIndexDefault
    protected set

  override var currentValue1: Value1Type = value1Default
    protected set
  override var currentValue2: Value2Type = value2Default
    protected set
  override var currentValue3: Value3Type = value3Default
    protected set
  override var currentValue4: Value4Type = value4Default
    protected set

  override var previousValue1: Value1Type = value1Default
    protected set
  override var previousValue2: Value2Type = value2Default
    protected set
  override var previousValue3: Value3Type = value3Default
    protected set
  override var previousValue4: Value4Type = value4Default
    protected set

  override var currentStartX: @Window Double = Double.NaN
    protected set
  override var currentEndX: @Window Double = Double.NaN
  override var currentStartTime: @Window Double = Double.NaN
    protected set
  override var currentEndTime: @Window Double = Double.NaN
  override var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

  override var nextValue1: @MayBeNoValueOrPending Value1Type = value1Default
  override var nextValue2: @MayBeNoValueOrPending Value2Type = value2Default
  override var nextValue3: @MayBeNoValueOrPending Value3Type = value3Default
  override var nextValue4: @MayBeNoValueOrPending Value4Type = value4Default

  override var nextStartX: @Window Double = Double.NaN
  override var nextEndX: @Window Double = Double.NaN
  override var nextStartTime: @Window Double = Double.NaN
  override var nextEndTime: @Window Double = Double.NaN

  override fun calculate(height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    reset()

    this.height = height
    this.visibleDataSeriesIndex = dataSeriesIndex
    this.historyConfiguration = historyConfiguration
  }

  /**
   * Prepares for the next value
   */
  override fun prepareForNextValue() {
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
    resetNext()
  }

  /**
   * Resets the next values to their defaults
   */
  fun resetNext() {
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
   * Reset all values
   */
  fun reset() {
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

    resetNext()
  }
}
