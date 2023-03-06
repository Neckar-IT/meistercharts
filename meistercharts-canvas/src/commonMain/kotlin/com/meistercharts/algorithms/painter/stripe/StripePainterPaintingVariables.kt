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

/**
 * Paining variables for stripe painters
 */
interface StripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, ValueType1, ValueType2, ValueType3> {
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
  val currentValue1: @MayBeNoValueOrPending ValueType1

  /**
   * The value2 that will be painted.
   */
  val currentValue2: @MayBeNoValueOrPending ValueType2

  /**
   * The value3 that will be painted.
   */
  val currentValue3: @MayBeNoValueOrPending ValueType3


  /**
   * The value1 that has been painted before the current value
   */
  val previousValue1: @MayBeNoValueOrPending ValueType1

  /**
   * The value2 that has been painted before the current value
   */
  val previousValue2: @MayBeNoValueOrPending ValueType2

  /**
   * The value3 that has been painted before the current value
   */
  val previousValue3: @MayBeNoValueOrPending ValueType3


  /**
   * The start location of the current stripe segment
   */
  val currentStartX: @Window Double

  /**
   * The end of the current segment.
   * This allows optimized painting (single rect of one color instead of multiple rects with the same color)
   */
  var currentEndX: @Window Double


  var nextValue1: @MayBeNoValueOrPending ValueType1
  var nextValue2: @MayBeNoValueOrPending ValueType2
  var nextValue3: @MayBeNoValueOrPending ValueType3

  var nextStartX: @Window Double
  var nextEndX: @Window Double
}


/**
 * Abstract base class
 */
abstract class AbstractStripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, ValueType1, ValueType2, ValueType3>(
  /**
   * Defaults value for data series index - will be set initially and on reset
   */
  val dataSeriesIndexDefault: DataSeriesIndexType,
  /**
   * The default value for value1* - will be set initially and on reset
   */
  val value1Default: ValueType1,
  /**
   * The default value for value2* - will be set initially and on reset
   */
  val value2Default: ValueType2,

  /**
   * The default value for value3 - will be set initially and on reset
   */
  val value3Default: ValueType3,

  ) : StripePainterPaintingVariables<DataSeriesIndexType, ValueType1, ValueType2, ValueType3> {

  override var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty
    protected set

  override var height: Double = Double.NaN
    protected set

  override var visibleDataSeriesIndex: DataSeriesIndexType = dataSeriesIndexDefault
    protected set

  override var currentValue1: ValueType1 = value1Default
    protected set
  override var currentValue2: ValueType2 = value2Default
    protected set
  override var currentValue3: ValueType3 = value3Default
    protected set

  override var previousValue1: ValueType1 = value1Default
    protected set
  override var previousValue2: ValueType2 = value2Default
    protected set
  override var previousValue3: ValueType3 = value3Default
    protected set

  override var currentStartX: @Window Double = Double.NaN
    protected set
  override var currentEndX: @Window Double = Double.NaN

  override var nextValue1: @MayBeNoValueOrPending ValueType1 = value1Default
  override var nextValue2: @MayBeNoValueOrPending ValueType2 = value2Default
  override var nextValue3: @MayBeNoValueOrPending ValueType3 = value3Default

  override var nextStartX: @Window Double = Double.NaN
  override var nextEndX: @Window Double = Double.NaN

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

    //Save next to current
    currentValue1 = nextValue1
    currentValue2 = nextValue2
    currentValue3 = nextValue3

    currentStartX = nextStartX
    currentEndX = nextEndX

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

    nextStartX = Double.NaN
    nextEndX = Double.NaN
  }


  /**
   * Reset all values
   */
  fun reset() {
    visibleDataSeriesIndex = dataSeriesIndexDefault

    currentValue1 = value1Default
    currentValue2 = value2Default
    currentValue3 = value3Default

    currentStartX = Double.NaN
    currentEndX = Double.NaN

    previousValue1 = value1Default
    previousValue2 = value2Default
    previousValue3 = value3Default

    resetNext()
  }
}
