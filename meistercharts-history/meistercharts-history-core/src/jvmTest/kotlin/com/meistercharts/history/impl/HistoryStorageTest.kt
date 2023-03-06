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
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryStorage
import org.junit.jupiter.api.Test

/**
 */
class HistoryStorageTest {
  @Test
  fun testMock() {
    val storage: HistoryStorage = EmptyHistoryStorage()

    val bucket = storage.get(HistoryBucketDescriptor.forTimestamp(100000000.0, HistoryBucketRange.OneHour))
    assertThat(bucket).isNull()
  }

  @Test
  internal fun testChildDescriptors() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(100000007.0, HistoryBucketRange.FiveSeconds)

    assertThat(descriptor.start).isEqualTo(100000000.0)
    assertThat(descriptor.end).isEqualTo(100000000.0 + 500 * 10)
    assertThat(descriptor.next().start).isEqualTo(descriptor.end)

    val children = descriptor.children()
    assertThat(children).isNotEmpty()
    assertThat(children.first().bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)

    assertThat(children.first().start).isEqualTo(descriptor.start)
    assertThat(children.last().end).isEqualTo(descriptor.end)
  }

  @Test
  internal fun testParent() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(100000007.0, HistoryBucketRange.OneMinute)

    descriptor.parent()!!.let {
      assertThat(it.contains(descriptor.start)).isTrue()
      assertThat(it.contains(descriptor.end)).isTrue()
      assertThat(it.bucketRange).isEqualTo(HistoryBucketRange.TenMinutes)

      assertThat(it.start).isEqualTo(9.96E7)
      assertThat(it.end).isEqualTo(1.002E8)

      assertThat(it.start / HistoryBucketRange.OneMinute.duration).isEqualTo(1660.0) //ensure it is a integer
    }
  }
}
