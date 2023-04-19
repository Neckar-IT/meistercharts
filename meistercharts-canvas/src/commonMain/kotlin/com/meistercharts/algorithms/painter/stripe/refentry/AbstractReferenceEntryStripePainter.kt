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
package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.stripe.AbstractStripePainter
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId

/**
 * Abstract base class for referenceEntry stripe painters
 */
abstract class AbstractReferenceEntryStripePainter : AbstractStripePainter<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, HistoryEnumSet, ReferenceEntryData?>(), ReferenceEntryStripePainter {
  /**
   * Provides the current aggregation mode
   */
  abstract val configuration: Configuration

  override fun paintingVariables(): ReferenceEntryStripePainterPaintingVariables {
    return paintingVariables
  }

  /**
   * The painting properties that are held
   */
  private val paintingVariables = DefaultReferenceEntryStripePainterPaintingVariables()


  override fun relevantValuesHaveChanged(value1: ReferenceEntryId, value2: ReferenceEntryDifferentIdsCount, value3: HistoryEnumSet, value4: ReferenceEntryData?): Boolean {
    val currentId = paintingVariables.currentValue1
    val currentCount = paintingVariables.currentValue2
    val currentEnumSet = paintingVariables.currentValue3
    val currentReferenceData = paintingVariables.currentValue4 //do not check - these values must never change for the same [ReferenceEntryId]

    return currentId != value1 || currentCount != value2 || currentEnumSet != value3
  }

  open class Configuration {
  }
}
