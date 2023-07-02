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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.loop.PaintingLoopIndex
import com.meistercharts.canvas.layout.cache.MappedLayoutCache
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration

/**
 * Painting variables for [StripePainter]s.
 *
 * This painting variables supports multiple data series by using [MappedLayoutCache] with the data series index as key
 */
abstract class StripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type>(
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

  ) : LoopIndexAware {

  /**
   * Contains the current loop index - required to be able to detect when the [paintingVariables4DataSeries] should be reset
   */
  override var loopIndex: PaintingLoopIndex = PaintingLoopIndex.Unknown

  /**
   * The current history configuration
   */
  var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty

  /**
   * Contains the delegate painting variables for each data series
   */
  private val paintingVariables4DataSeries = MappedLayoutCache<DataSeriesIndexType, StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type>> {
    StripePainterPaintingVariablesForOneDataSeries(dataSeriesIndexDefault, value1Default, value2Default, value3Default, value4Default)
  }

  /**
   * Returns the painting variables for the provided data series index
   */
  fun forDataSeriesIndex(dataSeriesIndex: DataSeriesIndexType): StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type> {
    return paintingVariables4DataSeries.get(dataSeriesIndex)
  }

  /**
   * Returns a string containg the counts of the segments
   */
  fun dumpSegments(): String {
    return buildString {
      paintingVariables4DataSeries.values.forEach { entry ->
        val key = entry.key
        val value = entry.value

        append(key.toString().padEnd(10))
        append("${value.segments.size} Segments")
        appendLine()
      }
    }
  }

  /**
   * This method is called once for *each* data series index.
   * Therefore, if there are multiple data series shown, this method is called multiple times per loop.
   */
  open fun prepareLayout(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    reset(paintingContext.loopIndex)
    this.historyConfiguration = historyConfiguration

    forDataSeriesIndex(dataSeriesIndex).prepareLayout(height, historyConfiguration, dataSeriesIndex)
  }

  /**
   * Reset all values
   */
  fun reset(loopIndex: PaintingLoopIndex) {
    this.loopIndex = loopIndex
    this.historyConfiguration = HistoryConfiguration.empty

    //Reset all - if it is a new loop index
    paintingVariables4DataSeries.resetIfNewLoopIndex(loopIndex)
  }
}

