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
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import it.neckar.open.collections.asDoubles
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 * Reproduces a down sampling bug related to corona dashboard
 */
class DownSamplingBugTest {
  @Test
  fun testBug() {
    val dataSeriesCount = 3

    val historyStorage = InMemoryHistoryStorage()
    val expectedSamplingPeriod = SamplingPeriod.EveryHour

    historyStorage.naturalSamplingPeriod = expectedSamplingPeriod
    historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(43200000 * 1000.0, expectedSamplingPeriod.toHistoryBucketRange())

    val timestamps = doubleArrayOf(
      1579647600000.0,
      1580252400000.0,
      1580857200000.0,
      1581462000000.0,
      1582066800000.0,
      1582671600000.0,
      1583276400000.0,
      1583881200000.0,
      1584486000000.0,
      1585090800000.0,
      1585692000000.0,
      1586296800000.0,
      1586901600000.0,
      1587506400000.0,
      1588111200000.0,
      1588716000000.0,
      1589320800000.0,
      1589925600000.0,
      1590530400000.0,
      1591135200000.0,
      1591740000000.0,
      1592344800000.0,
      1592949600000.0,
      1593554400000.0,
      1594159200000.0,
      1594764000000.0,
      1595368800000.0,
      1595973600000.0,
      1596578400000.0
    )
    val recovered = intArrayOf(28, 126, 1124, 5150, 16119, 30384, 51171, 67005, 83315, 116043, 195731, 325430, 509311, 709050, 970673, 1238857, 1539693, 1887486, 2327646, 2773422, 3445430, 4029616, 4696244, 5401296, 6511930, 7479335, 8545069, 9847460, 11207047).asDoubles()
    val confirmed = intArrayOf(555, 6167, 27637, 45223, 75642, 81451, 95181, 125950, 214939, 456486, 926095, 1470002, 2023684, 2579766, 3124774, 3707350, 4295733, 4936349, 5617475, 6430667, 7280801, 8251432, 9313253, 10510308, 11894293, 13411754, 15014754, 16820961, 18610735).asDoubles()
    val deaths = intArrayOf(17, 133, 564, 1118, 2122, 2770, 3254, 4615, 8733, 21035, 46413, 87706, 133354, 182569, 226771, 262746, 295713, 326662, 353296, 383273, 413913, 446320, 479888, 512788, 546437, 581197, 619241, 662733, 703022).asDoubles()

    check(timestamps.size == recovered.size && recovered.size == confirmed.size && confirmed.size == deaths.size)


    val chunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(1), TextKey.simple("recovered"))
      decimalDataSeries(DataSeriesId(2), TextKey.simple("confirmed"))
      decimalDataSeries(DataSeriesId(3), TextKey.simple("deaths"))
    }.chunk(timestamps, recovered, confirmed, deaths)

    historyStorage.storeWithoutCache(
      chunk, SamplingPeriod.EveryHour
    )


    val downSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()
    downSamplingDirtyRangesCollector.observe(historyStorage)
    historyStorage.downSamplingService.calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector)
  }

  @Test
  fun testSimple() {
    val dataSeriesCount = 1

    val historyStorage = InMemoryHistoryStorage()
    val expectedSamplingPeriod = SamplingPeriod.EveryHour

    historyStorage.naturalSamplingPeriod = expectedSamplingPeriod
    historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(43200000 * 1000.0, expectedSamplingPeriod.toHistoryBucketRange())

    val timestamps: DoubleArray = doubleArrayOf(
      1579647600000.0,
      1580252400000.0,
      1580857200000.0,
      1581462000000.0,
      1582066800000.0,
      1582671600000.0,
      1583276400000.0,
      1583881200000.0,
      1584486000000.0,
      1585090800000.0,
      1585692000000.0,
      1586296800000.0,
      1586901600000.0,
      1587506400000.0,
      1588111200000.0,
      1588716000000.0,
      1589320800000.0,
      1589925600000.0,
      1590530400000.0,
      1591135200000.0,
      1591740000000.0,
      1592344800000.0,
      1592949600000.0,
      1593554400000.0,
      1594159200000.0,
      1594764000000.0,
      1595368800000.0,
      1595973600000.0,
      1596578400000.0
    )

    val confirmed = intArrayOf(555, 6167, 27637, 45223, 75642, 81451, 95181, 125950, 214939, 456486, 926095, 1470002, 2023684, 2579766, 3124774, 3707350, 4295733, 4936349, 5617475, 6430667, 7280801, 8251432, 9313253, 10510308, 11894293, 13411754, 15014754, 16820961, 18610735).asDoubles()

    val chunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(3), TextKey.simple("confirmed"))
    }.chunk(timestamps, confirmed)

    historyStorage.storeWithoutCache(
      chunk, SamplingPeriod.EveryHour
    )

    val downSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()
    historyStorage.downSamplingService.calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector)
  }
}
