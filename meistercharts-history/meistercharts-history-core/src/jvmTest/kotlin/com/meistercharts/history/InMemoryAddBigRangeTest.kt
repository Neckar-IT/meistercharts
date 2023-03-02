package com.meistercharts.history

import com.meistercharts.history.impl.HistoryChunk
import it.neckar.open.i18n.TextKey
import com.meistercharts.history.impl.historyChunk
import org.junit.jupiter.api.Test

/**
 *
 */
class InMemoryAddBigRangeTest {
  @Test
  fun reproduces1050RequestedRangeTooBig() {
    val storage = InMemoryHistoryStorage()
    storage.naturalSamplingPeriod = SamplingPeriod.EveryHundredMillis

    val storageCache = HistoryStorageCache(storage)

    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(99), TextKey.simple("asdf"))
    }

    //Add one chung
    storageCache.scheduleForStore(historyChunk(historyConfiguration) {
      addDecimalValues(500.0, 1.0)
      addDecimalValues(1500.0, 2.0)
      //very far away!
      addDecimalValues(15_000_000.0, 1.0)
    }, SamplingPeriod.EveryHundredMillis)
  }
}
