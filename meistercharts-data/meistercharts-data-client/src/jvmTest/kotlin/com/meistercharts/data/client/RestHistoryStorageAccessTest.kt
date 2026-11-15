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
package com.meistercharts.data.client

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.time.TimeRange
import com.meistercharts.time.TimeRanges
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import it.neckar.open.collections.cache
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RestHistoryStorageAccessTest {
  lateinit var mockClient: DataClient
  lateinit var storageAccess: RestHistoryStorageAccess

  val range: HistoryBucketRange = HistoryBucketRange.HundredMillis
  val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)

  @OptIn(ExperimentalCoroutinesApi::class)
  @BeforeEach
  fun setUp() {
    mockClient = mockk<DataClient>(relaxed = true)
    storageAccess = RestHistoryStorageAccess(mockClient)

    Dispatchers.setMain(StandardTestDispatcher())
  }

  @AfterEach
  fun tearDown() {
    storageAccess.dispose()
  }

  @Test
  fun testGetWithLaunch() = runTest {
    storageAccess = RestHistoryStorageAccess(mockClient, this)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)

    coEvery {
      mockClient.get(descriptor)
    } returns mockBucket
    assertThat(storageAccess.get(descriptor)).isNull()
    assertThat(storageAccess.currentlyGetting).contains(descriptor)

    delay(10) // delay to separate the assertions

    coVerify(exactly = 1) {// client.get must only be called once!!
      mockClient.get(any())
    }
    assertThat(storageAccess.get(descriptor)).isNotNull()
  }

  @Test
  suspend fun testGetUpdate(): Unit = coroutineScope {
    storageAccess = RestHistoryStorageAccess(mockClient, this)

    val slotDescriptor = slot<HistoryBucketDescriptor>()

    coEvery {
      mockClient.getUpdates()
    } returns mutableListOf(HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, TimeRanges(listOf(TimeRange(1000.0, 2000.0)))))

    coEvery {
      mockClient.get(capture(slotDescriptor))
    } returns MockSinusHistoryStorage().get(descriptor)

    assertThat(storageAccess.getUpdateDescriptors()).isNotNull()
  }

  /**
   * A failed request must not block its descriptor forever: the next [RestHistoryStorageAccess.get] has to ask the server again.
   */
  @Test
  fun testFailedRequestIsRetried() = runTest {
    val requestFailures = mutableListOf<Throwable>()
    storageAccess = RestHistoryStorageAccess(
      dataClient = mockClient,
      requestScope = CoroutineScope(StandardTestDispatcher(testScheduler) + SupervisorJob() + CoroutineExceptionHandler { _, thrown -> requestFailures.add(thrown) }),
    )

    coEvery {
      mockClient.get(descriptor)
    } throws RuntimeException("server not reachable")

    assertThat(storageAccess.get(descriptor)).isNull()
    advanceUntilIdle()

    assertThat(requestFailures).hasSize(1)
    assertThat(storageAccess.currentlyGetting).doesNotContain(descriptor)

    coEvery {
      mockClient.get(descriptor)
    } returns MockSinusHistoryStorage().get(descriptor)

    assertThat(storageAccess.get(descriptor)).isNull()
    advanceUntilIdle()

    coVerify(exactly = 2) { // the failed request must have been repeated
      mockClient.get(descriptor)
    }
    assertThat(storageAccess.get(descriptor)).isNotNull()
  }

  @Test
  fun testCacheOldValue() {
    val cache = cache<HistoryBucketDescriptor, HistoryBucket?>("CachedRemoteHistoryStorage", 100)
    val oldValue = cache.store(descriptor, null)
    assertThat(oldValue).isEqualTo(null)
  }
}



