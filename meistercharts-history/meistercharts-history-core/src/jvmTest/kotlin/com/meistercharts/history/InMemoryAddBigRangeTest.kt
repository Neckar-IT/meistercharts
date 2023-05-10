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
