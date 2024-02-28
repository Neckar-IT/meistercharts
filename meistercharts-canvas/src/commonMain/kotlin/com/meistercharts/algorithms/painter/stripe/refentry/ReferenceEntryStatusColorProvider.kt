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

import com.meistercharts.color.Color
import com.meistercharts.design.Theme
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId

/**
 * Provides the colors for the status
 */
fun interface ReferenceEntryStatusColorProvider {
  /**
   * Returns the color for the given reference entry id
   */
  fun color(dataSeriesIndex: ReferenceEntryDataSeriesIndex, value: ReferenceEntryId, statusEnumSet: HistoryEnumSet, historyConfiguration: HistoryConfiguration): Color

  companion object {
    /**
     * Creates a new instance of the default implementation
     */
    fun default(): ReferenceEntryStatusColorProvider {
      return ReferenceEntryStatusColorProvider { _, _, statusEnumSet, _ ->
        when {
          statusEnumSet.isNoValue() -> {
            Color.silver()
          }

          statusEnumSet.isPending() -> {
            Color.lightgray()
          }

          else -> {
            Theme.enumColors.resolve().valueAt(statusEnumSet.firstSetOrdinal().value)
          }
        }
      }
    }
  }
}
