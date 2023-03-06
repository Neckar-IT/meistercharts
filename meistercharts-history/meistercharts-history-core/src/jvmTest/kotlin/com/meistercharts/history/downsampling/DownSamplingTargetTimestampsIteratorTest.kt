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

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import org.junit.jupiter.api.Test

/**
 *
 */
class DownSamplingTargetTimestampsIteratorTest {
  @Test
  fun testIterator() {
    val bucketDescriptor = HistoryBucketDescriptor.forTimestamp(10_000_000.0, HistoryBucketRange.HundredMillis)

    assertThat(bucketDescriptor.start).isEqualTo(10_000_000.0)
    assertThat(bucketDescriptor.end).isEqualTo(10_000_000.0 + 100)


    val iterator = DownSamplingTargetTimestampsIterator.create(bucketDescriptor)

    assertThat(iterator.index.value).isEqualTo(0)
    assertThat(iterator.distance).all {
      isEqualTo(bucketDescriptor.bucketRange.distance)
      isEqualTo(1.0)
    }

    assertThat(iterator.slotCenter).isEqualTo(10_000_000.0 + 0.5)

    assertThat(iterator.slotStart).isEqualTo(10_000_000.0)
    assertThat(iterator.slotEnd).isEqualTo(10_000_000.0 + 1)
  }
}
