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

import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms
import kotlin.math.sin

class MockWritableHistoryStorage(val fileStorage: WritableHistoryStorage? = null): WritableHistoryStorage {
  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(10), TextKey("val1", "Value 1"), HistoryUnit("kg"))
    decimalDataSeries(DataSeriesId(11), TextKey("val2", "Value 2"), HistoryUnit("cm"))
    decimalDataSeries(DataSeriesId(12), TextKey("val3", "Value 3"), HistoryUnit("C"))
  }

  override fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo) {
  }

  override fun delete(descriptor: HistoryBucketDescriptor) {
  }

  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket {
    val chunk = createMockChunk(descriptor)
    val historyBucket = HistoryBucket(descriptor, chunk)
    fileStorage?.storeWithoutCache(historyBucket, HistoryUpdateInfo.fromChunk(chunk, descriptor.bucketRange.samplingPeriod))
    return historyBucket
  }

  override fun onDispose(action: () -> Unit) {
  }

  /**
   * Returns a mock history chunk for the given descriptor
   * demo bug at TenMinutes bucket range
   */
  fun createMockChunk(descriptor: HistoryBucketDescriptor): HistoryChunk {
    val timestampsCount = descriptor.bucketRange.entriesCount
    @ms val distance = descriptor.bucketRange.samplingPeriod.distance

    return historyConfiguration.chunk(timestampsCount) { timestampIndex ->
      @ms val timestamp = descriptor.start + distance * timestampIndex.value
      addDecimalValues(
        timestamp = timestamp, (sin(timestamp / 1_000.0) * 110), (sin(timestamp / 10_300.0) * 92 + 10), (sin(timestamp / 100_000.0) * 91 + 20)
        //timestamp = timestamp, (timestamp % 1000).toDouble()
      )
    }
  }
}


