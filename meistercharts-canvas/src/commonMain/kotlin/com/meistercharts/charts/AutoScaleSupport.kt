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
package com.meistercharts.charts

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.IntermediateValuesMode
import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.canvas.timerSupport
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.SamplingPeriod
import it.neckar.open.collections.fastMapNotNull
import it.neckar.open.kotlin.lang.findMagnitudeValue
import it.neckar.open.kotlin.lang.findMagnitudeValueCeil
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.Sorted
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Helper class for auto scale
 */
class AutoScaleSupport(
  /**
   * The gestalt that is updated
   */
  val gestalt: TimeLineChartGestalt,

  /**
   * The default value ranges. Will be used as fallback
   */
  val defaultValueRanges: MultiProvider<DecimalDataSeriesIndex, ValueRange>,

  /**
   * Intermediate values mode
   */
  val intermediateValuesMode: IntermediateValuesMode = IntermediateValuesMode.Also5and2,
) {

  /**
   * Is set to true if auto-scaling has to be recalculated
   */
  private var dirty: Boolean = false

  /**
   * Automatically update auto-scaling if necessary
   */
  fun repeatAutoScaleRecalculation(
    /**
     * The chart support is used for timer and to extract the necessary information
     */
    chartSupport: ChartSupport,
    /**
     * The data series indices auto-scale is applied for
     */
    dataSeriesIndices: @Sorted List<DecimalDataSeriesIndex>,
    /**
     * The window for recalculation (at most)
     */
    checkWindow: Duration = 500.milliseconds,
  ) {
    //Listen to changes to the history
    val observableHistoryStorage = gestalt.data.historyStorage as ObservableHistoryStorage
    observableHistoryStorage.observe { _, _ ->
      dirty = true
    }

    //Listen to changes to the visible area
    gestalt.style.contentAreaTimeRangeProperty.consume {
      dirty = true
    }

    chartSupport.timerSupport.repeat(checkWindow) {
      if (dirty) {
        recalculateAutoScale(chartSupport, dataSeriesIndices)
        dirty = false
      }
    }
  }

  /**
   * Calculates auto-scale for the given data series index
   */
  fun recalculateAutoScale(
    chartSupport: ChartSupport,
    /**
     * The data series indices that are updated. Must be sorted by data series index
     */
    dataSeriesIndices: @Sorted List<DecimalDataSeriesIndex>,
  ) {
    val currentSamplingPeriod = chartSupport.paintingProperties.retrieveOrNull(PaintingPropertyKey.SamplingPeriod) ?: return

    val visibleTimeRange = chartSupport.chartCalculator.visibleTimeRangeXinWindow(gestalt.style.contentAreaTimeRange)

    //Contains the updated value ranges. The position of the value range corresponds to the index of the data series index
    val updatedValueRanges = createUpdatedValueRanges(visibleTimeRange, currentSamplingPeriod, dataSeriesIndices)

    val oldProvider = gestalt.style.lineValueRanges

    if (containsChanges(updatedValueRanges, oldProvider)) {
      gestalt.style.lineValueRanges = MultiProvider.invoke {
        updatedValueRanges[DecimalDataSeriesIndex(it)] ?: this.defaultValueRanges.valueAt(it)
      }
    }
  }

  /**
   * Returns true if the updated value ranges contain differences compared to the old value ranges
   */
  private fun containsChanges(updatedValueRanges: Map<DecimalDataSeriesIndex, LinearValueRange>, oldProvider: MultiProvider<DecimalDataSeriesIndex, ValueRange>): Boolean {
    updatedValueRanges.forEach {
      val dataSeriesIndex = it.key
      val updatedValueRange = it.value

      val oldRange = oldProvider.valueAt(dataSeriesIndex.value)

      if (updatedValueRange != oldRange) {
        return true
      }
    }

    return false
  }

  /**
   * Creates a list of updated value ranges
   */
  fun createUpdatedValueRanges(
    visibleTimeRange: TimeRange,
    currentSamplingPeriod: SamplingPeriod,
    dataSeriesIndices: List<DecimalDataSeriesIndex>,
  ): Map<DecimalDataSeriesIndex, LinearValueRange> {
    val minMaxValues = gestalt.data.historyStorage.queryMinMax(visibleTimeRange, currentSamplingPeriod, dataSeriesIndices)

    return dataSeriesIndices.fastMapNotNull { dataSeriesIndex ->
      val min = minMaxValues.min(dataSeriesIndex)
      val max = minMaxValues.max(dataSeriesIndex)

      if (min != null && max != null && min != max) {
        createWidenedValueRange(min, max)?.let {
          Pair(dataSeriesIndex, it)
        }
      } else {
        null
      }
    }.toMap()
  }

  /**
   * Creates the value range for the given min/max values.
   * The min/max values will be widened to "rounded" values
   */
  fun createWidenedValueRange(min: @Domain Double, max: @Domain Double): LinearValueRange? {
    if (min <= 0.0 || max <= 0.0) {
      //widening not supported for negative values
      return ValueRange.linear(min, max)
    }

    val minWidened = intermediateValuesMode.findLarger(min.findMagnitudeValue()) {
      it <= min
    }
    val maxWidened = intermediateValuesMode.findSmaller(max.findMagnitudeValueCeil()) {
      it >= max
    }

    if (minWidened == maxWidened) {
      return null
    }

    return ValueRange.linear(minWidened, maxWidened)
  }
}

/**
 * Installs auto-scale support for this gestalt
 */
fun TimeLineChartGestalt.installAutoScaleSupport(
  chartSupport: ChartSupport,
  dataSeriesIndices: @Sorted List<DecimalDataSeriesIndex>,
  defaultValueRanges: MultiProvider<DecimalDataSeriesIndex, ValueRange>,
): AutoScaleSupport {
  val autoScaleSupport = AutoScaleSupport(this, defaultValueRanges)
  autoScaleSupport.repeatAutoScaleRecalculation(chartSupport, dataSeriesIndices)
  return autoScaleSupport
}
