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
import com.meistercharts.data.rest.MeisterchartsDataEndpoints
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.rest.HistoryBucketList
import com.meistercharts.history.storage.FileHistoryStorage
import com.meistercharts.time.TimeRange
import io.ktor.http.*
import io.ktor.server.testing.*
import it.neckar.rest.test.*
import it.neckar.ktor.client.getJson
import it.neckar.ktor.client.putJson
import it.neckar.open.http.Url
import it.neckar.open.test.utils.DisableLogging
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@DisableLogging
class DataServerRoutingITTest {
  val serverUrl = Url.localhostHttp(80)

  lateinit var historyStorage: FileHistoryStorage
  val clientUpdateSupport = ClientUpdatesSupport.create()

  @BeforeEach
  fun setup(@TempDir newTempDirectory: File) {
    val baseDir = newTempDirectory
    val json: Json = Json {
      HistoryConfiguration
    }
    historyStorage = FileHistoryStorage(baseDir, json)

    val range = HistoryBucketRange.HundredMillis
    val range2 = HistoryBucketRange.OneDay
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val descriptor1: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516001E12, range)
    val mockBucket1 = MockSinusHistoryStorage().get(descriptor1)
    val descriptor2: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516002E12, range)
    val mockBucket2 = MockSinusHistoryStorage().get(descriptor2)
    val descriptor3: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516003E12, range)
    val mockBucket3 = MockSinusHistoryStorage().get(descriptor3)
    val descriptor4: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range2)
    val mockBucket4 = MockSinusHistoryStorage().get(descriptor4)
    historyStorage.storeWithoutCache(mockBucket, HistoryUpdateInfo(mockBucket.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket.start, mockBucket.end)))
    historyStorage.storeWithoutCache(mockBucket1, HistoryUpdateInfo(mockBucket1.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket1.start, mockBucket1.end)))
    historyStorage.storeWithoutCache(mockBucket2, HistoryUpdateInfo(mockBucket2.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket2.start, mockBucket2.end)))
    historyStorage.storeWithoutCache(mockBucket3, HistoryUpdateInfo(mockBucket3.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket3.start, mockBucket3.end)))
    historyStorage.storeWithoutCache(mockBucket4, HistoryUpdateInfo(mockBucket4.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket4.start, mockBucket4.end)))
  }

  @Test
  fun testGet(): Unit = testApplication {
    application {
      historyRouting(historyStorage, clientUpdateSupport)
      installJsonSupportForTests()
    }/* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }
    val historyBucketRange = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, historyBucketRange)
    when (val receivedBucket: HistoryBucket = client.getJson(serverUrl, MeisterchartsDataEndpoints.History.bucketUrl(descriptor.bucketRange, descriptor.start))) {
      is HistoryBucket -> {
        assertThat(receivedBucket.start).isEqualTo(descriptor.start)
      }
    }
  }

  @Test
  fun testPost(): Unit = testApplication {
    application {
      historyRouting(historyStorage, ClientUpdatesSupport.create())
      installJsonSupportForTests()
    }
    /* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }

    val range = HistoryBucketRange.FiveYears
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)

    val receivedHttpResponse = client.putJson(serverUrl, MeisterchartsDataEndpoints.History.bucketsStoreUrl(), mockBucket)
    assertThat(receivedHttpResponse).isCreated()
  }

  /** Adds the last history bucket for downscaling.
   */
  @Test
  fun testPostDownscaling(): Unit = testApplication {
    application {
      historyRouting(historyStorage, clientUpdateSupport)
      installJsonSupportForTests()
    }
    /* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }

    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516004E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)

    val receivedHttpResponse = client.putJson(serverUrl, MeisterchartsDataEndpoints.History.bucketsStoreUrl(), mockBucket)
    assertThat(receivedHttpResponse).isCreated()
  }

  @Test
  fun testQuery(): Unit = testApplication {
    application {
      historyRouting(historyStorage, clientUpdateSupport)
      installJsonSupportForTests()
    }
    /* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }

    val receivedBucketList: HistoryBucketList = client.getJson(serverUrl, MeisterchartsDataEndpoints.History.queryUrl(HistoryBucketRange.HundredMillis, 1.5927516E12, 1.5927516003E12))
    assertThat(receivedBucketList).isNotNull()

    assertThat(receivedBucketList.buckets).isNotNull()
    assertThat(receivedBucketList.buckets[0].chunk.values.decimalHistoryValues.values).isNotNull()
  }
}

