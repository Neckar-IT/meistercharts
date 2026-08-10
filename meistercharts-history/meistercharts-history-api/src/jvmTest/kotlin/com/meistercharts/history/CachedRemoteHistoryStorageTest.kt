/*
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
import com.meistercharts.history.impl.MockSinusHistoryStorage
import org.junit.jupiter.api.Test

class CachedRemoteHistoryStorageTest {
  private val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(100000.0, SamplingPeriod.EveryTenMillis)

  @Test
  fun `returns the bucket if the async access answers immediately`() {
    val remote = MockSinusHistoryStorage()
    var queryCount = 0

    val storage = CachedRemoteHistoryStorage { requested, consumer ->
      queryCount++
      consumer(remote.get(requested))
    }

    assertThat(storage.get(descriptor)?.start).isEqualTo(descriptor.start)
    assertThat(queryCount).isEqualTo(1)

    //the second call is answered from the cache
    assertThat(storage.get(descriptor)?.start).isEqualTo(descriptor.start)
    assertThat(queryCount).isEqualTo(1)
  }

  @Test
  fun `returns null as long as the async access has not answered`() {
    val remote = MockSinusHistoryStorage()
    var pendingConsumer: ((HistoryBucket) -> Unit)? = null

    val storage = CachedRemoteHistoryStorage { _, consumer ->
      pendingConsumer = consumer
    }

    assertThat(storage.get(descriptor)).isNull()

    //answer the pending query - the bucket is available from now on
    pendingConsumer?.invoke(remote.get(descriptor))
    assertThat(storage.get(descriptor)?.start).isEqualTo(descriptor.start)
  }
}
