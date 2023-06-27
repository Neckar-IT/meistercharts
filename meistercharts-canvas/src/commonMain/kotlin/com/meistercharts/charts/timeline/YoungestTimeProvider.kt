package com.meistercharts.charts.timeline

import com.meistercharts.history.HistoryStorage
import it.neckar.open.kotlin.lang.ifNaN
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.si.ms

/**
 * Provides the youngest time from a history storage
 */
class YoungestTimeProvider(
  /**
   * The history storage that is used to extract the time from
   */
  val historyStorage: HistoryStorage,
  /**
   * The fallback is used, if the history storage does not provide a youngest time
   */
  val fallback: @ms DoubleProvider = DoubleProvider.nowMillis,
) : @ms DoubleProvider {
  override fun invoke(): Double {
    return historyStorage.getEnd().ifNaN { fallback() }
  }
}

