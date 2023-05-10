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

import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.TimestampIndex
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.i18n.I18nConfiguration

fun main() {
  val storage = MockSinusHistoryStorage()
  val bucket = storage.get(HistoryBucketDescriptor.forTimestamp(1000000.0, HistoryBucketRange.OneMinute))

  for (i in 0 until bucket.chunk.timeStampsCount) {
    val time = bucket.chunk.timestampCenter(TimestampIndex(i))
    val v0 = bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(i))
    val v1 = bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(i))
    val v2 = bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(i))

    println("Time: ${dateTimeFormat.format(time, I18nConfiguration.Germany)} - $v0 $v1 $v2")
  }
}
