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

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryObserveTest {
  @Test
  fun testIt() {
    val historyStorage = InMemoryHistoryStorage()

    val updateInfos = mutableListOf<HistoryUpdateInfo>()

    historyStorage.observe { updateInfo ->
      updateInfos.add(updateInfo)
    }

    assertThat(updateInfos).isEmpty()

    val newChunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey.simple("dasdf"))
    }.chunk() {
      addDecimalValues(124000.0, 7.70)
    }

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(7.70)
    historyStorage.naturalSamplingPeriod = SamplingPeriod.EveryHour
    historyStorage.storeWithoutCache(newChunk, SamplingPeriod.EveryHour)

    assertThat(updateInfos).hasSize(1)
  }
}
