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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms
import kotlin.math.sin

/**
 * Mock implementation that returns a new instance every time that contains sinus values
 */
class MockSinusHistoryStorage : HistoryStorage {
  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket {
    return HistoryBucket(descriptor, createSinusChunk(descriptor))
  }

  override fun onDispose(action: () -> Unit) {
  }

  companion object {
    /**
     * The value range this history storage creates values for
     */
    val valueRange: ValueRange = ValueRange.linear(-110.0, 110.0)
  }
}

/**
 * Returns a history chunk for the given descriptor that contains three data series each with a sin
 */
fun createSinusChunk(descriptor: HistoryBucketDescriptor): HistoryChunk {
  val timestampsCount = descriptor.bucketRange.entriesCount
  @ms val distance = descriptor.bucketRange.samplingPeriod.distance

  return historyConfiguration {
    decimalDataSeries(DataSeriesId(10), TextKey("val1", "Value 1"), HistoryUnit("kg"))
    decimalDataSeries(DataSeriesId(11), TextKey("val2", "Value 2"), HistoryUnit("cm"))
    decimalDataSeries(DataSeriesId(12), TextKey("val3", "Value 3"), HistoryUnit.None)
  }.chunk(timestampsCount) { timestampIndex ->
    @ms val timestamp = descriptor.start + distance * timestampIndex.value
    addDecimalValues(
      timestamp, (sin(timestamp / 1_000.0) * 100), (sin(timestamp / 10_000.0) * 95 + 10), (sin(timestamp / 100_000.0) * 90 + 20)
    )
  }
}
