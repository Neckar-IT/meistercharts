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
package com.meistercharts.server

import assertk.*
import assertk.assertions.*
import com.meistercharts.data.client.DataClient
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.storage.FileHistoryStorage
import com.meistercharts.time.TimeRange
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.neckar.rest.test.*
import it.neckar.open.http.Url
import it.neckar.open.test.utils.DisableLogging
import it.neckar.open.test.utils.SlowTest
import org.junit.jupiter.api.Test

@DisableLogging
@SlowTest
class DataClientITTest {
  val mockStorage: FileHistoryStorage = mockk<FileHistoryStorage>(relaxed = true)
  val range: HistoryBucketRange = HistoryBucketRange.HundredMillis
  val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
  val mockBucket: HistoryBucket = MockSinusHistoryStorage().get(descriptor)


  @Test
  fun testGet(): Unit = testApplication {
    val slotDescriptor = slot<HistoryBucketDescriptor>()
    every { mockStorage.get(capture(slotDescriptor)) } answers {
      MockSinusHistoryStorage().get(descriptor)
    }

    application {
      historyRouting(mockStorage, ClientUpdatesSupport.create())
      installJsonSupportForTests()
    }

    val client = DataClient(createClient {
      setupTestClient(jwtConfiguration = null)
    }, Url.localhostHttp(80))
    val receivedBucket = client.get(descriptor.bucketRange, descriptor.start)

    if (receivedBucket != null) {
      assertThat(receivedBucket.start).isEqualTo(descriptor.start)
      assertThat(receivedBucket.bucketRange).isEqualTo(descriptor.bucketRange)
      assertThat(receivedBucket.descriptor.end).isEqualTo(descriptor.end)
    }

    verify {
      mockStorage.get(descriptor)
    }
  }

  @Test
  fun `get(descriptor) returns null when the storage has no bucket`(): Unit = testApplication {
    //The server always responds with HistoryBucketRestResponse(descriptor, chunk?). When the storage
    //has no data, chunk is null and the client must return null - not throw a deserialization error.
    every { mockStorage.get(any<HistoryBucketDescriptor>()) } returns null

    application {
      historyRouting(mockStorage, ClientUpdatesSupport.create())
      installJsonSupportForTests()
    }

    val client = DataClient(createClient {
      setupTestClient(jwtConfiguration = null)
    }, Url.localhostHttp(80))

    val receivedBucket = client.get(descriptor)
    assertThat(receivedBucket).isNull()
  }

  @Test
  fun testQuery(): Unit = testApplication {
    every { mockStorage.query(any(), any(), any()) } returns listOf(mockBucket)

    application {
      historyRouting(mockStorage, ClientUpdatesSupport.create())
      installJsonSupportForTests()
    }

    val client = DataClient(createClient {
      setupTestClient(jwtConfiguration = null)
    }, Url.localhostHttp(80))
    val receivedBucketList = client.query(descriptor.bucketRange, descriptor.start, descriptor.end)

    assertThat(receivedBucketList.buckets.size).isEqualTo(1)
    assertThat(receivedBucketList.buckets[0].start).isEqualTo(descriptor.start)

    verify {
      mockStorage.query(descriptor.start, descriptor.end, descriptor.bucketRange.samplingPeriod)
    }

  }


  @Test
  fun testPost(): Unit = testApplication {
    val slotBucket = slot<HistoryBucket>()
    every { mockStorage.storeWithoutCache(capture(slotBucket), any()) } answers {}

    application {
      historyRouting(mockStorage, ClientUpdatesSupport.create())
      installJsonSupportForTests()
    }
    val client = DataClient(createClient {
      setupTestClient(jwtConfiguration = null)
    }, Url.localhostHttp(80))
    val answer = client.put(mockBucket)

    assertThat(answer).isCreated()

    verify {
      mockStorage.storeWithoutCache(mockBucket, HistoryUpdateInfo(mockBucket.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket.start, mockBucket.end)))
    }
  }
}
