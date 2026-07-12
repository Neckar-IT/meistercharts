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
import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariables
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum

/**
 * Painting variables for enum stripes
 */
open class EnumStripePainterPaintingVariables :
  StripePainterPaintingVariables<EnumDataSeriesIndex, EnumStripePainterPaintingVariablesForOneDataSeries>(
    dataSeriesIndexDefault = EnumDataSeriesIndex.zero,
  ) {
  /**
   * The current history enum for the visible data series index
   */
  var historyEnum: HistoryEnum = HistoryEnum.Boolean
    private set

  override fun createForOneDataSeries(): EnumStripePainterPaintingVariablesForOneDataSeries {
    return EnumStripePainterPaintingVariablesForOneDataSeries()
  }

  override fun prepareLayout(paintingContext: LayerPaintingContext, height: Double, dataSeriesIndex: EnumDataSeriesIndex, historyConfiguration: HistoryConfiguration) {
    super.prepareLayout(paintingContext, height, dataSeriesIndex, historyConfiguration)
    historyEnum = getHistoryEnum(dataSeriesIndex)
  }

  /**
   * Returns the history enum value for the given index
   */
  fun getHistoryEnum(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnum {
    return historyConfiguration.enumConfiguration.getEnum(dataSeriesIndex)
  }
}
