package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.design.Theme
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntryId

/**
 * Provides the color for a status enum
 */
fun interface ReferenceEntryStatusColorProvider {
  /**
   * Returns the color for the given reference entry id
   */
  fun color(value: ReferenceEntryId, statusEnumSet: HistoryEnumSet, historyConfiguration: HistoryConfiguration): Color

  companion object {
    /**
     * Creates a new instance of the default implementation
     */
    fun default(): ReferenceEntryStatusColorProvider {
      return ReferenceEntryStatusColorProvider { _, statusEnumSet, _ ->
        when {
          statusEnumSet.isNoValue() -> {
            Color.silver
          }

          statusEnumSet.isPending() -> {
            Color.lightgray
          }

          else -> {
            Theme.enumColors().valueAt(statusEnumSet.firstSetOrdinal().value)
          }
        }
      }
    }
  }
}
