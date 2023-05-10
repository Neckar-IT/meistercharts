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

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 *
 */
class HistoryBucketDescriptorPerformanceDemo {
  @Test
  fun testPerformance() {

    for (run in 0..10) {
      measureTimeMillis {
        for (i in 0..1_000_000) {
          val descriptor = HistoryBucketDescriptor.forTimestamp(777777777777.0, HistoryBucketRange.OneMinute)
          descriptor.start
          descriptor.end
          descriptor.timeRange
        }
      }.also { println("Took $it ms") }

      //25-35 ms with old implementation
    }
  }
}