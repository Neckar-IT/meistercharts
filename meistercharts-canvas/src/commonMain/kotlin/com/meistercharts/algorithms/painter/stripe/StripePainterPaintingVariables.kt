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

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LoopIndexAware
import com.meistercharts.algorithms.layers.LoopIndexAwarePaintingVariables
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.cache.LayoutVariable
import com.meistercharts.canvas.layout.cache.LayoutVariableObjectCache
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Paining variables for stripe painters
 */
interface StripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type> : LoopIndexAware {
  /**
   * Calculates the values. This method will be called initially before/when the painter is used.
   * Afterward [prepareForNextValue] is called for each new value.
   */
  fun prepareLayout(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration)

  /**
   * Prepares for the next value.
   *
   * This method might be called multiple times for each painter.
   * This method must only be called after [prepareLayout] has been called once.
   */
  fun prepareForNextValue()

  /**
   * Is called during the layout phase to store the next segment that will then be painted in the painting phase
   */
  fun storeSegment(startX: @Window Double, endX: @Window Double, activeTimeStamp: @ms @MayBeNaN Double, value1ToPaint: Value1Type, value2ToPaint: Value2Type, value3ToPaint: Value3Type, value4ToPaint: Value4Type)

  /**
   * Contains the information about the segments.
   * Is filled by calling [storeSegment]
   */
  val segments: LayoutVariableObjectCache<SegmentLayoutVariables<Value1Type, Value2Type, Value3Type, Value4Type>>

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
