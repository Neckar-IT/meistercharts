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
package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.historyConfiguration
import it.neckar.open.unit.other.Inclusive
import org.junit.jupiter.api.Test

class HistoryChunkMergeBugTest {

  /**
   * Reproduces a bug that has been detected when integrating into the plotter component
   */
  @Test
  fun testMergeBug() {
    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(7), "pressure")
    }

    /*
     * This is the debug output from the browser:
     */

    //add timestamps for [1665040360000, 1665040370000] = [2022-10-06T07:12:40.000Z, Thu Oct 06 2022 09:12:50 GMT+0200 (Central European Summer Time)]
    //lazyLoading.html:140 about to add 100 samples
    //Plotter.ts?ad56:763 the data-series configuration must be updated; the history will likely be cleared
    //lazyLoading.html:125 add timestamps for [1665040352100, 1665040362100] = [2022-10-06T07:12:32.100Z, Thu Oct 06 2022 09:12:42 GMT+0200 (Central European Summer Time)]
    //lazyLoading.html:140 about to add 100 samples
    //lazyLoading.html:125 add timestamps for [1665040345900, 1665040355900] = [2022-10-06T07:12:25.900Z, Thu Oct 06 2022 09:12:35 GMT+0200 (Central European Summer Time)]
    //lazyLoading.html:140 about to add 100 samples
    //lazyLoading.html:125 add timestamps for [1665040342800, 1665040352800] = [2022-10-06T07:12:22.800Z, Thu Oct 06 2022 09:12:32 GMT+0200 (Central European Summer Time)]
    //lazyLoading.html:140 about to add 100 samples
    //lazyLoading.html:125 add timestamps for [1665040344600, 1665040354600] = [2022-10-06T07:12:24.600Z, Thu Oct 06 2022 09:12:34 GMT+0200 (Central European Summer Time)]
    //lazyLoading.html:140 about to add 100 samples
    //lazyLoading.html:125 add timestamps for [1665040311600, 1665040321600] = [2022-10-06T07:11:51.600Z, Thu Oct 06 2022 09:12:01 GMT+0200 (Central European Summer Time)]
    //lazyLoading.html:140 about to add 100 samples


    val storage = InMemoryHistoryStorage()

    assertThat(createChunk(historyConfiguration, 1665040360000, 1665040370000).timeStampsCount).isEqualTo(101)

    storage.storeWithoutCache(createChunk(historyConfiguration, 1665040360000, 1665040370000), SamplingPeriod.EveryHundredMillis)
    //storage.storeWithoutCache(createChunk(historyConfiguration, 1665040352100, 1665040362100), SamplingPeriod.EveryHundredMillis)
    //storage.storeWithoutCache(createChunk(historyConfiguration, 1665040345900, 1665040355900), SamplingPeriod.EveryHundredMillis)
    //storage.storeWithoutCache(createChunk(historyConfiguration, 1665040342800, 1665040352800), SamplingPeriod.EveryHundredMillis)
    //storage.storeWithoutCache(createChunk(historyConfiguration, 1665040344600, 1665040354600), SamplingPeriod.EveryHundredMillis)
    storage.storeWithoutCache(createChunk(historyConfiguration, 1665040311600, 1665040321600), SamplingPeriod.EveryHundredMillis)


  }

  //TODO attention! Creating 101 elements
  private fun createChunk(historyConfiguration: HistoryConfiguration, start: @Inclusive Long, end: @Inclusive Long): HistoryChunk {
    val chunkToStore = historyChunk(historyConfiguration) {
      for (millis in start..end step 100) {
        this.addDecimalValues(millis.toDouble(), 100.0)
      }
    }
    return chunkToStore
  }
}
