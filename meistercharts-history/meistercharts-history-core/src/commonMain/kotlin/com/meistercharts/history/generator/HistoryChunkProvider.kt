package com.meistercharts.history.generator

import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.impl.HistoryChunk
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms

/**
 * Provides history chunks
 * //TODO: continue!
 */
interface HistoryChunkProvider {

  val historyConfiguration: HistoryConfiguration

  /**
   * Creates the next [HistoryChunk]
   */
  fun next(until: @ms Double = nowMillis()): HistoryChunk?

  companion object {
  }
}
