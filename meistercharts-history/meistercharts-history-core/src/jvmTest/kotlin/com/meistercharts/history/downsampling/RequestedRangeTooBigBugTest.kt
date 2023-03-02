package com.meistercharts.history.downsampling

import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test
import kotlin.math.sin


class RequestedRangeTooBigBugTest {

  @Test
  fun testIt() {
    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val historyStorage = InMemoryHistoryStorage().apply {
      naturalSamplingPeriod = samplingPeriod
    }
    val downSamplingService = historyStorage.downSamplingService

    @ms val firstTimestamp = 1622648264896.0
    @ms val distance = 701230.0
    val chunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey("val1", "Value 1"))
    }.chunk(862) { timestampIndex ->
      @ms val timestamp = firstTimestamp + distance * timestampIndex.value
      addDecimalValues(
        timestamp,
        (sin(timestamp / 1_000.0) * 100)
      )
    }

    val downSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()
    downSamplingDirtyRangesCollector.observe(historyStorage)
    historyStorage.storeWithoutCache(chunk, samplingPeriod)
    downSamplingService.calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector)
  }
}
