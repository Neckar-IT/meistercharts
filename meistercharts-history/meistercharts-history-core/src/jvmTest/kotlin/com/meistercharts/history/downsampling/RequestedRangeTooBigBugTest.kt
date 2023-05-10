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
